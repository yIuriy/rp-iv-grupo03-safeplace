// Testes da tela /relatorios (RF18, US18, issue #299): pré-visualização mensal, download anual em CSV,
// mensagem da API quando o arquivo falha e acesso do Supervisor. Somente as respostas HTTP são simuladas.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

// Formato de `RelatorioResponse`.
const relatorioAcidentes = {
  tipo: 'ACIDENTES',
  titulo: 'Relatório de acidentes e incidentes',
  inicio: '2026-10-01',
  fim: '2026-10-31',
  geradoEm: '2026-11-11T14:30:00',
  secoes: [
    {
      titulo: 'Resumo',
      indicadores: [
        { rotulo: 'Total de ocorrências', valor: '5' },
        { rotulo: 'Acidentes', valor: '3' },
        { rotulo: 'Incidentes', valor: '2' },
      ],
      tabela: null,
    },
    {
      titulo: 'Ocorrências por área de risco',
      indicadores: [],
      tabela: {
        colunas: ['Área de risco', 'Acidentes', 'Incidentes', 'Total'],
        linhas: [['SET-01 · Caldeiras', '2', '1', '3'], ['SET-02 · Pátio', '1', '1', '2']],
        semDados: 'Nenhuma ocorrência no período.',
      },
    },
    {
      titulo: 'Acidentes por gravidade',
      indicadores: [],
      tabela: { colunas: ['Gravidade', 'Quantidade'], linhas: [], semDados: 'Nenhum acidente no período.' },
    },
  ],
}

const anoCorrente = String(new Date().getFullYear())

async function simularApi(page: Page, opcoes: { erroArquivo?: string; semRegistros?: boolean } = {}) {
  const pedidos: URL[] = []
  await page.route('**/api/relatorios/**', route => {
    const url = new URL(route.request().url())
    pedidos.push(url)
    if (url.pathname.endsWith('/arquivo')) {
      if (opcoes.erroArquivo) return route.fulfill({ status: 400, json: { message: opcoes.erroArquivo } })
      return route.fulfill({
        status: 200,
        contentType: 'text/csv;charset=UTF-8',
        headers: { 'Content-Disposition': 'attachment; filename="relatorio-acidentes-2025-01-01-a-2025-12-31.csv"' },
        body: '﻿Relatório de acidentes e incidentes\r\n',
      })
    }
    return route.fulfill({ json: { ...relatorioAcidentes, semRegistros: opcoes.semRegistros ?? false } })
  })
  return pedidos
}

test.describe('Relatórios estatísticos (RF18, US18, #299)', () => {
  test('Gestor pré-visualiza o relatório do ano corrente com indicadores e tabelas', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const pedidos = await simularApi(page)
    await page.goto('/relatorios')

    await expect(page.getByRole('heading', { name: 'Relatórios', level: 1 })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Escolha o relatório e o período' })).toBeVisible()
    await expect(page.getByLabel('Ano')).toHaveValue(anoCorrente)

    await page.getByRole('button', { name: 'Pré-visualizar' }).click()

    const relatorio = page.getByRole('article', { name: 'Relatório de acidentes e incidentes' })
    await expect(relatorio).toBeVisible()
    await expect(relatorio.getByText('Período de 01/10/2026 a 31/10/2026; gerado em 11/11/2026 às 14:30.')).toBeVisible()
    await expect(relatorio.getByRole('term').filter({ hasText: 'Total de ocorrências' })).toBeVisible()
    await expect(relatorio.getByRole('definition').first()).toHaveText('5')
    const tabela = relatorio.getByRole('table', { name: 'Ocorrências por área de risco' })
    await expect(tabela.getByRole('row')).toHaveCount(3)
    await expect(tabela.getByRole('row').nth(1)).toContainText('SET-01 · Caldeiras')
    await expect(relatorio.getByText('Nenhum acidente no período.')).toBeVisible()

    expect(pedidos).toHaveLength(1)
    expect(pedidos[0].pathname).toBe('/api/relatorios/acidentes')
    expect(pedidos[0].searchParams.get('inicio')).toBe(`${anoCorrente}-01-01`)
    expect(pedidos[0].searchParams.get('fim')).toBe(`${anoCorrente}-12-31`)
    await expect(relatorio.getByText('Nenhum registro neste período.')).toHaveCount(0)
  })

  test('Período sem registros mostra aviso; o recorte mensal envia o mês inteiro', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const pedidos = await simularApi(page, { semRegistros: true })
    await page.goto('/relatorios')

    await page.getByLabel('Período').selectOption('mensal')
    await page.getByLabel('Mês').fill('2026-02')
    await page.getByRole('button', { name: 'Pré-visualizar' }).click()

    await expect(page.getByText('Nenhum registro neste período.')).toBeVisible()
    expect(pedidos[0].searchParams.get('inicio')).toBe('2026-02-01')
    expect(pedidos[0].searchParams.get('fim')).toBe('2026-02-28')
  })

  test('Gestor baixa o relatório anual de treinamentos em CSV', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const pedidos = await simularApi(page)
    await page.goto('/relatorios')

    await page.getByLabel('Relatório', { exact: true }).selectOption({ label: 'Treinamentos e certificações' })
    await page.getByLabel('Período').selectOption('anual')
    await page.getByLabel('Ano').fill('2025')
    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Baixar CSV' }).click()

    expect((await download).suggestedFilename()).toBe('relatorio-acidentes-2025-01-01-a-2025-12-31.csv')
    await expect(page.getByText('Relatório baixado: relatorio-acidentes-2025-01-01-a-2025-12-31.csv.')).toBeVisible()
    const pedido = pedidos.at(-1)!
    expect(pedido.pathname).toBe('/api/relatorios/treinamentos/arquivo')
    expect(Object.fromEntries(pedido.searchParams)).toEqual({ inicio: '2025-01-01', fim: '2025-12-31', formato: 'CSV' })
  })

  test('Ano inválido é barrado na tela, sem chamar a API', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const pedidos = await simularApi(page)
    await page.goto('/relatorios')

    await page.getByLabel('Período').selectOption('anual')
    await page.getByLabel('Ano').fill('1990')
    await page.getByRole('button', { name: 'Baixar PDF' }).click()

    await expect(page.getByText('Informe um ano entre 2000 e 2100.')).toBeVisible()
    expect(pedidos).toHaveLength(0)
  })

  test('Falha na geração do arquivo mostra a mensagem da API', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { erroArquivo: 'O período do relatório pode ter no máximo um ano.' })
    await page.goto('/relatorios')

    await page.getByRole('button', { name: 'Baixar PDF' }).click()

    const alerta = page.getByRole('alert').filter({ hasText: 'O arquivo não foi gerado.' })
    await expect(alerta).toContainText('O período do relatório pode ter no máximo um ano.')
  })

  test('Supervisor acessa Relatórios pelo menu', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page)
    await page.goto('/usuarios')

    await page.getByRole('link', { name: 'Relatórios' }).first().click()
    await expect(page).toHaveURL(/\/relatorios$/)
    await expect(page.getByRole('heading', { name: 'Relatórios', level: 1 })).toBeVisible()
  })
})
