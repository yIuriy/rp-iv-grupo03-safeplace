// Testes do histórico de alterações (#114): o Gestor abre o histórico no detalhe da área e na linha
// da tarefa e vê data, operador e o que mudou; o Supervisor não vê a opção; falha de rede tem aviso.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

const area = {
  id: 1,
  codigo: 'SET-01',
  nome: 'Linha de Solda',
  descricao: null,
  nivelPerigo: 'ALTO',
  setorId: null,
  agentesRisco: ['Ruído', 'Calor'],
  limites: 'Entre as linhas A e C',
  episObrigatorios: [{ id: 1, nome: 'Capacete de segurança', numeroCa: '12345', status: 'DISPONIVEL' }],
}

const tarefa = {
  id: 7,
  descricao: 'Corte a quente de tubulação',
  nivelPerigo: 'ALTO',
  classificada: true,
  dataClassificacao: '2026-10-02T09:10:00',
  responsavelClassificacaoNome: 'Gestora Ana',
}

// Formato de `RegistroHistoricoResponse`, do mais recente ao mais antigo, como a API devolve.
const historicoArea = [
  {
    id: 12,
    tipoOperacao: 'ALTERACAO',
    dataHora: '2026-10-02T14:30:00',
    responsavel: 'gestora@safeplace.test',
    descricao: 'Alteração da área de risco: limites: de "Entre as linhas A e B" para "Entre as linhas A e C". Justificativa: Revisão anual',
  },
  {
    id: 9,
    tipoOperacao: 'ALTERACAO',
    dataHora: '2026-09-28T08:05:00',
    responsavel: 'gestora@safeplace.test',
    descricao: 'Alteração da área de risco: agentes de risco: de "Ruído" para "Ruído, Calor".',
  },
]

const historicoTarefa = [
  {
    id: 31,
    tipoOperacao: 'ALTERACAO',
    dataHora: '2026-10-02T09:10:00',
    responsavel: 'gestora@safeplace.test',
    descricao: 'Classificação de periculosidade da tarefa: de MEDIO para ALTO; responsável técnico: Gestora Ana (id 1)',
  },
  {
    id: 30,
    tipoOperacao: 'CADASTRO',
    dataHora: '2026-09-30T16:00:00',
    responsavel: 'gestora@safeplace.test',
    descricao: 'Cadastro de Tarefa: Corte a quente de tubulação',
  },
]

async function simularApi(page: Page, opcoes: { historicoAreaFalha?: boolean } = {}) {
  const consultasHistorico: string[] = []
  await page.route('**/api/setores', route => route.fulfill({ json: [] }))
  await page.route('**/api/epis', route => route.fulfill({ json: [] }))
  await page.route('**/api/areas-risco', route => route.fulfill({ json: [area] }))
  await page.route('**/api/tarefas', route => route.fulfill({ json: [tarefa] }))
  await page.route('**/api/areas-risco/*/historico', route => {
    consultasHistorico.push(new URL(route.request().url()).pathname)
    return opcoes.historicoAreaFalha
      ? route.fulfill({ status: 500, json: { message: 'falha' } })
      : route.fulfill({ json: historicoArea })
  })
  await page.route('**/api/tarefas/*/historico', route => {
    consultasHistorico.push(new URL(route.request().url()).pathname)
    return route.fulfill({ json: historicoTarefa })
  })
  return consultasHistorico
}

async function abrirDetalheDaArea(page: Page) {
  await page.goto('/areas-risco')
  await page.locator('article', { hasText: 'SET-01 - Linha de Solda' })
    .getByRole('button', { name: /Consultar área/ }).click()
  const dialog = page.getByRole('dialog', { name: 'SET-01 - Linha de Solda' })
  await expect(dialog).toBeVisible()
  return dialog
}

test.describe('Histórico de alterações de áreas e tarefas (#114, UC03, UC11)', () => {
  test('Gestor abre o histórico da área sob demanda e vê data, operador e o que mudou', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simularApi(page)
    const dialog = await abrirDetalheDaArea(page)

    expect(consultas).toEqual([])
    const alternar = dialog.getByRole('button', { name: 'Ver histórico de alterações' })
    await expect(alternar).toHaveAttribute('aria-expanded', 'false')
    await alternar.click()

    const historico = dialog.getByRole('region', { name: 'Histórico de alterações da área' })
    await expect(historico.getByRole('listitem')).toHaveCount(2)
    const maisRecente = historico.getByRole('listitem').first()
    await expect(maisRecente.getByText('02/10/2026 às 14:30')).toBeVisible()
    await expect(maisRecente.getByRole('heading', { name: 'Alteração por gestora@safeplace.test' })).toBeVisible()
    await expect(maisRecente.getByText(/de "Entre as linhas A e B" para "Entre as linhas A e C"/)).toBeVisible()
    await expect(maisRecente.getByText(/Justificativa: Revisão anual/)).toBeVisible()
    expect(consultas).toEqual(['/api/areas-risco/1/historico'])

    const ocultar = dialog.getByRole('button', { name: 'Ocultar histórico de alterações' })
    await expect(ocultar).toHaveAttribute('aria-expanded', 'true')
    await ocultar.click()
    await expect(historico).toBeHidden()
  })

  test('falha ao carregar o histórico mostra aviso e permite tentar novamente', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simularApi(page, { historicoAreaFalha: true })
    const dialog = await abrirDetalheDaArea(page)

    await dialog.getByRole('button', { name: 'Ver histórico de alterações' }).click()
    await expect(dialog.getByRole('alert')).toContainText('Não foi possível carregar o histórico.')
    await dialog.getByRole('button', { name: 'Tentar novamente' }).click()
    await expect.poll(() => consultas.length).toBe(2)
  })

  test('Supervisor consulta a área sem a opção de histórico', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const consultas = await simularApi(page)
    const dialog = await abrirDetalheDaArea(page)

    await expect(dialog.getByRole('button', { name: 'Ver histórico de alterações' })).toHaveCount(0)
    expect(consultas).toEqual([])
  })

  test('Gestor abre o histórico de classificações da tarefa pela linha da matriz', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simularApi(page)
    await page.goto('/tarefas')

    await page.getByRole('button', { name: 'Ver histórico da tarefa: Corte a quente de tubulação' }).click()
    const dialog = page.getByRole('dialog', { name: 'Histórico: Corte a quente de tubulação' })
    await expect(dialog).toBeVisible()

    const itens = dialog.getByRole('listitem')
    await expect(itens).toHaveCount(2)
    await expect(itens.first().getByText(/de MEDIO para ALTO; responsável técnico: Gestora Ana/)).toBeVisible()
    await expect(itens.last().getByRole('heading', { name: 'Cadastro por gestora@safeplace.test' })).toBeVisible()
    expect(consultas).toEqual(['/api/tarefas/7/historico'])

    await dialog.getByRole('button', { name: 'Fechar histórico' }).click()
    await expect(dialog).not.toBeVisible()
  })

  test('Supervisor não vê a opção de histórico na matriz de tarefas', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page)
    await page.goto('/tarefas')

    await expect(page.getByText('Corte a quente de tubulação')).toBeVisible()
    await expect(page.getByRole('button', { name: /Ver histórico da tarefa/ })).toHaveCount(0)
  })
})
