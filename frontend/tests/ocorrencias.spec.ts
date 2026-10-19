// Testes da consulta de ocorrências (UC05/UC09, issues #128 e #195): critérios enviados ao servidor,
// inclusive tipo e gravidade, refinamento local, período invertido, detalhe com data do fato e do
// registro, relator, protocolo CAT, detalhe fora do perfil, teclado e larguras. Só as respostas HTTP
// são simuladas.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

// Formato de `OcorrenciaResponse` do backend.
const acidente = {
  idOcorrencia: 1, tipoOcorrencia: 'ACIDENTE', dataOcorrencia: '2026-09-10T14:37:00', dataRegistro: '2026-09-10T15:02:00',
  statusOcorrencia: 'ABERTA', descricao: 'Prensa atingiu a mão do operador.', local: null, numeroProtocoloCAT: 'CAT-2026-09-0001',
  gravidade: 'CRITICO', causaRaiz: 'FALHA_EPI', tipo: 'Esmagamento', dano: 'Lesão na mão direita', destino: 'Hospital Municipal',
  situacaoRisco: null, potencialDano: null, areaId: 10, areaNome: 'Linha de Prensas', colaboradorId: 7,
  colaboradorNome: 'João da Linha', relatorId: 1, relatorNome: 'Gestora Ana', relatorPerfil: 'GESTOR_SEGURANCA',
}
const incidente = {
  idOcorrencia: 2, tipoOcorrencia: 'INCIDENTE', dataOcorrencia: '2026-08-20T09:00:00', dataRegistro: '2026-08-21T08:00:00',
  statusOcorrencia: 'EM_TRIAGEM', descricao: 'Óleo no corredor da doca.', local: null, numeroProtocoloCAT: null, gravidade: null,
  causaRaiz: null, tipo: null, dano: null, destino: null, situacaoRisco: 'Piso escorregadio', potencialDano: 'Queda',
  areaId: 11, areaNome: 'Doca de Carga', colaboradorId: 8, colaboradorNome: 'Maria Souza', relatorId: 9, relatorNome: 'Joana Ribeiro',
  relatorPerfil: 'SUPERVISOR',
}
const setores = [{ id: 1, nome: 'Galpão Principal', descricao: null, localizacao: 'Bloco 1', status: 'ALTO' }]
const colaboradores = [
  { id: 7, nome: 'João da Linha', cpf: '12345678909', dataNascimento: '1992-06-18', email: null, perfil: null, ativo: true },
  { id: 8, nome: 'Maria Souza', cpf: '52998224725', dataNascimento: '1990-01-01', email: null, perfil: null, ativo: true },
]

type Opcoes = { detalheIndisponivel?: number }

/** Simula a API: a listagem aplica os critérios como o servidor, para provar o que a tela envia. */
async function simular(page: Page, opcoes: Opcoes = {}) {
  const consultas: URLSearchParams[] = []
  const registros = [acidente, incidente]
  await page.route('**/api/setores', route => route.fulfill({ json: setores }))
  await page.route('**/api/usuarios/colaboradores**', route => route.fulfill({ json: colaboradores }))
  await page.route('**/api/usuarios', route => route.fulfill({ json: colaboradores }))
  await page.route(url => url.pathname.startsWith('/api/ocorrencias'), route => {
    const url = new URL(route.request().url())
    const protocolo = url.pathname.match(/\/api\/ocorrencias\/protocolo\/(.+)$/)
    if (protocolo) {
      const achado = registros.find(r => r.numeroProtocoloCAT === decodeURIComponent(protocolo[1]))
      return achado ? route.fulfill({ json: achado }) : route.fulfill({ status: 404, json: { message: 'Não encontrada' } })
    }
    const porId = url.pathname.match(/\/api\/ocorrencias\/(\d+)$/)
    if (porId) {
      const id = Number(porId[1])
      const achado = registros.find(r => r.idOcorrencia === id)
      return achado && id !== opcoes.detalheIndisponivel
        ? route.fulfill({ json: achado })
        : route.fulfill({ status: 404, json: { message: 'Não encontrada' } })
    }
    consultas.push(url.searchParams)
    const p = url.searchParams
    const dataFato = (r: typeof acidente | typeof incidente) => r.dataOcorrencia.slice(0, 10)
    const setorDaArea: Record<number, number> = { 10: 1, 11: 2 }
    return route.fulfill({ json: registros.filter(r =>
      (!p.get('dataInicio') || dataFato(r) >= p.get('dataInicio')!)
      && (!p.get('dataFim') || dataFato(r) <= p.get('dataFim')!)
      && (!p.get('setorId') || setorDaArea[r.areaId] === Number(p.get('setorId')))
      && (!p.get('colaboradorId') || r.colaboradorId === Number(p.get('colaboradorId')))
      && (!p.get('tipoOcorrencia') || r.tipoOcorrencia === p.get('tipoOcorrencia'))
      && (!p.get('gravidade') || r.gravidade === p.get('gravidade'))) })
  })
  return consultas
}

const consulta = (page: Page) => page.getByRole('search', { name: 'Consultar ocorrências' })
const linha = (page: Page, id: number) => page.getByRole('row', { name: new RegExp(`^${id} `) })

test.describe('Consulta de ocorrências (UC05/UC09, issue #128)', () => {
  test('período, setor e colaborador vão ao servidor e a lista mostra só o que atende', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simular(page)
    await page.goto('/ocorrencias')
    await expect(linha(page, 2)).toBeVisible()

    const form = consulta(page)
    await form.getByLabel('Data do fato: de').fill('2026-09-01')
    await form.getByLabel('Data do fato: até').fill('2026-09-30')
    await form.getByLabel('Setor').selectOption({ label: 'Galpão Principal (Bloco 1)' })
    await form.getByLabel('Colaborador envolvido').selectOption({ label: 'João da Linha' })
    await form.getByRole('button', { name: 'Consultar' }).click()

    await expect(linha(page, 2)).toHaveCount(0)
    await expect(linha(page, 1)).toBeVisible()
    expect(Object.fromEntries(consultas.at(-1)!)).toEqual({
      dataInicio: '2026-09-01', dataFim: '2026-09-30', setorId: '1', colaboradorId: '7',
    })
  })

  test('combinação sem resultado é informada e Limpar volta à consulta completa', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simular(page)
    await page.goto('/ocorrencias')

    const form = consulta(page)
    await form.getByLabel('Setor').selectOption({ label: 'Galpão Principal (Bloco 1)' })
    await form.getByLabel('Colaborador envolvido').selectOption({ label: 'Maria Souza' })
    await form.getByRole('button', { name: 'Consultar' }).click()
    await expect(page.getByRole('heading', { name: 'Nenhuma ocorrência atende aos critérios' })).toBeVisible()

    await form.getByRole('button', { name: 'Limpar' }).click()
    await expect(linha(page, 1)).toBeVisible()
    await expect(linha(page, 2)).toBeVisible()
    expect(consultas.at(-1)!.toString()).toBe('')
    await expect(form.getByLabel('Setor')).toHaveValue('')
  })

  test('período invertido é barrado antes de consultar, com foco na data final', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simular(page)
    await page.goto('/ocorrencias')
    await expect(linha(page, 1)).toBeVisible()
    const total = consultas.length

    const form = consulta(page)
    await form.getByLabel('Data do fato: de').fill('2026-09-30')
    await form.getByLabel('Data do fato: até').fill('2026-09-01')
    await form.getByRole('button', { name: 'Consultar' }).click()

    await expect(form.getByText('A data final deve ser igual ou posterior à data inicial.')).toBeVisible()
    await expect(form.getByLabel('Data do fato: até')).toBeFocused()
    await expect(form.getByLabel('Data do fato: até')).toHaveAttribute('aria-invalid', 'true')
    expect(consultas.length).toBe(total)
  })

  test('texto e situação refinam a lista recebida sem critério novo no servidor', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simular(page)
    await page.goto('/ocorrencias')
    await expect(linha(page, 1)).toBeVisible()

    const form = consulta(page)
    await form.getByLabel('Situação').selectOption('EM_TRIAGEM')
    await form.getByLabel('Buscar na lista').fill('doca')
    await form.getByRole('button', { name: 'Consultar' }).click()

    await expect(linha(page, 1)).toHaveCount(0)
    await expect(linha(page, 2)).toBeVisible()
    await expect(page.getByRole('status').filter({ hasText: 'ocorrência encontrada' })).toHaveText('1 ocorrência encontrada')
    expect(consultas.at(-1)!.toString()).toBe('')
  })

  test('tipo e gravidade vão ao servidor (issue #195)', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const consultas = await simular(page)
    await page.goto('/ocorrencias')
    await expect(linha(page, 2)).toBeVisible()

    const form = consulta(page)
    await form.getByLabel('Tipo').selectOption('INCIDENTE')
    await form.getByRole('button', { name: 'Consultar' }).click()
    await expect(linha(page, 1)).toHaveCount(0)
    await expect(linha(page, 2)).toBeVisible()
    expect(Object.fromEntries(consultas.at(-1)!)).toEqual({ tipoOcorrencia: 'INCIDENTE' })

    await form.getByLabel('Tipo').selectOption('')
    await form.getByLabel('Gravidade').selectOption('CRITICO')
    await form.getByRole('button', { name: 'Consultar' }).click()
    await expect(linha(page, 2)).toHaveCount(0)
    await expect(linha(page, 1)).toBeVisible()
    expect(Object.fromEntries(consultas.at(-1)!)).toEqual({ gravidade: 'CRITICO' })
  })

  test('detalhe do acidente separa data do fato e data do registro e devolve o foco ao fechar', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page)
    await page.goto('/ocorrencias')

    const botao = page.getByRole('button', { name: 'Ver detalhes da ocorrência 1' })
    await botao.focus()
    await page.keyboard.press('Enter')
    const dialogo = page.getByRole('dialog', { name: 'Ocorrência #1' })
    const valor = (rotulo: string) => dialogo.locator('dl > div').filter({ has: page.locator('dt', { hasText: new RegExp(`^${rotulo}$`) }) }).locator('dd')
    await expect(valor('Data do fato')).toHaveText('10/09/2026 14:37')
    await expect(valor('Data do registro')).toHaveText('10/09/2026 15:02')
    await expect(valor('Protocolo CAT')).toHaveText('CAT-2026-09-0001')
    await expect(valor('Gravidade')).toHaveText('Crítico')
    await expect(valor('Causa raiz')).toHaveText('Falha de EPI')
    await expect(valor('Área de risco')).toHaveText('Linha de Prensas')
    await expect(valor('Colaborador envolvido')).toHaveText('João da Linha')
    await expect(valor('Registrado por')).toHaveText('Gestora Ana (Gestor de Segurança)')

    await page.keyboard.press('Escape')
    await expect(dialogo).not.toBeVisible()
    await expect(botao).toBeFocused()
  })

  test('detalhe do incidente mostra situação de risco e potencial de dano, sem protocolo', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page)
    await page.goto('/ocorrencias')

    await page.getByRole('button', { name: 'Ver detalhes da ocorrência 2' }).click()
    const dialogo = page.getByRole('dialog', { name: 'Ocorrência #2' })
    await expect(dialogo.getByText('Piso escorregadio')).toBeVisible()
    await expect(dialogo.getByText('Queda')).toBeVisible()
    await expect(dialogo.getByText('Protocolo CAT')).toHaveCount(0)
    await expect(dialogo.getByText('Joana Ribeiro (Supervisor)')).toBeVisible()
    await expect(linha(page, 2)).toContainText('Sem protocolo')
  })

  test('protocolo CAT encontrado abre o detalhe; inexistente gera aviso', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page)
    await page.goto('/ocorrencias')
    const busca = page.getByRole('search', { name: 'Consultar acidente pelo protocolo CAT' })

    await busca.getByRole('button', { name: 'Consultar protocolo' }).click()
    await expect(busca.getByText('Informe o protocolo CAT.')).toBeVisible()

    await busca.getByLabel('Protocolo CAT').fill('CAT-2026-09-0001')
    await busca.getByRole('button', { name: 'Consultar protocolo' }).click()
    await expect(page.getByRole('dialog', { name: 'Ocorrência #1' })).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page.getByRole('dialog', { name: 'Ocorrência #1' })).not.toBeVisible()

    await busca.getByLabel('Protocolo CAT').fill('CAT-2026-09-9999')
    await busca.getByRole('button', { name: 'Consultar protocolo' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Protocolo não encontrado' }))
      .toContainText('Nenhum acidente com o protocolo CAT-2026-09-9999')
  })

  test('Supervisor recebe aviso claro quando o detalhe não está disponível ao perfil', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simular(page, { detalheIndisponivel: 2 })
    await page.goto('/ocorrencias')
    await expect(page.getByText('Relate acidentes e incidentes e acompanhe os relatos que você registrou.')).toBeVisible()

    await page.getByRole('button', { name: 'Ver detalhes da ocorrência 2' }).click()
    await expect(page.getByRole('dialog', { name: 'Ocorrência #2' }).getByRole('status'))
      .toContainText('não está entre os relatos que seu perfil pode consultar')
  })

  for (const width of [360, 768, 1344]) {
    test(`consulta e detalhe cabem em ${width} px sem rolagem horizontal`, async ({ page }) => {
      await page.setViewportSize({ width, height: 900 })
      await iniciarSessao(page, gestora)
      await simular(page)
      await page.goto('/ocorrencias')
      await expect(linha(page, 1)).toBeVisible()
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

      await page.getByRole('button', { name: 'Ver detalhes da ocorrência 1' }).click()
      await expect(page.getByRole('dialog', { name: 'Ocorrência #1' })).toBeVisible()
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    })
  }
})
