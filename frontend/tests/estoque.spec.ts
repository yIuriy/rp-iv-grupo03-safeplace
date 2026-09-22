// Testes Playwright da tela de estoque de EPIs (/estoque) (issues #123 e #133): catálogo com situação crítica textual,
// detalhes com histórico e operador, matriz de perfis (só o Gestor movimenta), registro de entrada
// com atualização de saldo e histórico, recusa de saída acima do saldo mantendo os dados, validação
// local, teclado e largura móvel. Somente as respostas HTTP são simuladas.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, pessoas, supervisor } from './helpers/sessao'

const capacete = {
  id: 1, nome: 'Capacete classe B', numeroCa: '12345', quantidade: 10, estoqueMinimo: 5, estoqueCritico: false,
  status: 'DISPONIVEL', dataValidadeCa: '2027-12-31', vidaUtilDias: 365, descricao: 'Capacete com aba frontal',
  classificacao: 'PROTECAO_DE_CABECA', localizacao: 'Almoxarifado A',
}
const luva = {
  id: 2, nome: 'Luva nitrílica', numeroCa: '777', quantidade: 5, estoqueMinimo: 5, estoqueCritico: true,
  status: 'DISPONIVEL', dataValidadeCa: '2027-06-30', vidaUtilDias: 180, descricao: null,
  classificacao: 'PROTECAO_DE_MEMBROS_SUPERIORES', localizacao: null,
}
const historicoCapacete = [
  { id: 11, epiId: 1, loteId: 1, responsavelId: 1, tipo: 'SAIDA', quantidade: 4, dataHora: '2026-09-20T14:05:00', motivo: 'Entrega à equipe da obra', saldoAtual: 10 },
  { id: 10, epiId: 1, loteId: 1, responsavelId: null, tipo: 'ENTRADA', quantidade: 14, dataHora: '2026-09-19T09:30:00', motivo: 'Recebimento de lote', saldoAtual: 14 },
]

type Chamada = { method: string; url: string; body: unknown }

/** Simula catálogo, histórico, usuário do operador e a movimentação; registra as chamadas de escrita. */
async function simularEstoque(page: Page, opcoes: { movimentacao?: { status: number; json: unknown } } = {}) {
  const chamadas: Chamada[] = []
  let epis = [capacete, luva]
  await page.route('**/api/epis', route => route.fulfill({ json: epis }))
  await page.route('**/api/epis/1/movimentacoes', route => {
    const request = route.request()
    if (request.method() === 'GET') return route.fulfill({ json: historicoCapacete })
    chamadas.push({ method: request.method(), url: request.url(), body: request.postDataJSON() })
    const resposta = opcoes.movimentacao ?? {
      status: 201,
      json: { id: 12, epiId: 1, loteId: 1, responsavelId: 1, tipo: 'ENTRADA', quantidade: 5, dataHora: '2026-09-21T10:00:00', motivo: 'Reposição', saldoAtual: 15 },
    }
    if (resposta.status === 201) epis = [{ ...capacete, quantidade: 15 }, luva]
    return route.fulfill(resposta)
  })
  await page.route('**/api/epis/2/movimentacoes', route => route.fulfill({ json: [] }))
  await page.route('**/api/usuarios/1', route => route.fulfill({ json: pessoas.gestora }))
  return chamadas
}

test('catalog shows balance, minimum and a textual critical badge, not only a color', async ({ page }) => {
  await iniciarSessao(page, gestora)
  await simularEstoque(page)
  await page.goto('/estoque')

  const tabela = page.getByRole('table', { name: 'Catálogo de EPIs em estoque' })
  await expect(tabela.getByRole('row')).toHaveCount(3)
  const linhaLuva = tabela.getByRole('row', { name: /Luva nitrílica/ })
  await expect(linhaLuva.getByText('Crítico', { exact: true })).toBeVisible()
  await expect(linhaLuva.getByRole('cell', { name: '5', exact: true })).toHaveCount(2)
  const linhaCapacete = tabela.getByRole('row', { name: /Capacete classe B/ })
  await expect(linhaCapacete.getByText('Crítico', { exact: true })).toHaveCount(0)
  await expect(linhaCapacete.getByText('Disponível', { exact: true })).toBeVisible()
  await expect(linhaCapacete.getByRole('cell', { name: 'Almoxarifado A', exact: true })).toBeVisible()
  await expect(page.getByRole('status').filter({ hasText: '1 EPI com estoque crítico' })).toBeVisible()
  await expect(page.getByText('código de barras')).toHaveCount(0)
})

test('details dialog lists history with operator, date and time, quantity, reason and balance', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  await simularEstoque(page)
  await page.goto('/estoque')

  await page.getByRole('button', { name: 'Detalhes e histórico de Capacete classe B' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Capacete classe B' })
  await expect(dialogo).toBeVisible()
  await expect(dialogo.getByText('10 (mínimo 5)')).toBeVisible()
  await expect(dialogo.getByText('Proteção de cabeça')).toBeVisible()
  const historico = dialogo.getByRole('list', { name: 'Histórico de movimentações' })
  await expect(historico.getByRole('listitem')).toHaveCount(2)
  const saida = historico.getByRole('listitem').first()
  await expect(saida.getByRole('heading', { name: 'Saída de 4 unidades' })).toBeVisible()
  await expect(saida.getByText('20/09/2026 14:05')).toBeVisible()
  await expect(saida.getByText('Operador: Gestora Ana. Motivo: Entrega à equipe da obra. Saldo após: 10.')).toBeVisible()
  await expect(historico.getByText('Operador: Não identificado. Motivo: Recebimento de lote. Saldo após: 14.')).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(dialogo).not.toBeVisible()
})

test('Supervisor consults stock but has no movement action', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  await simularEstoque(page)
  await page.goto('/estoque')

  await expect(page.getByRole('table', { name: 'Catálogo de EPIs em estoque' })).toBeVisible()
  await expect(page.getByText('Entradas e saídas são registradas pelo Gestor de Segurança.')).toBeVisible()
  await expect(page.getByRole('button', { name: /Movimentar estoque/ })).toHaveCount(0)
  await expect(page.getByRole('button', { name: /Detalhes e histórico/ })).toHaveCount(2)
})

test('Gestor registers an entry: request body, toast, refreshed balance and history', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularEstoque(page)
  await page.goto('/estoque')

  await page.getByRole('button', { name: 'Movimentar estoque de Capacete classe B' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Registrar movimentação' })
  await expect(dialogo.getByText('saldo atual 10, mínimo 5')).toBeVisible()
  await expect(dialogo.getByRole('radio', { name: 'Entrada (reposição)' })).toBeChecked()
  await dialogo.getByLabel('Quantidade').fill('5')
  await dialogo.getByLabel('Motivo').fill('Reposição')
  await dialogo.getByRole('button', { name: 'Registrar' }).click()

  await expect(dialogo).not.toBeVisible()
  expect(chamadas).toHaveLength(1)
  expect(chamadas[0].method).toBe('POST')
  expect(chamadas[0].body).toEqual({ tipo: 'ENTRADA', quantidade: 5, motivo: 'Reposição' })
  await expect(page.getByRole('status').filter({ hasText: 'Entrada de 5 unidades registrada para Capacete classe B. Saldo atual: 15.' })).toBeVisible()
  const linha = page.getByRole('row', { name: /Capacete classe B/ })
  await expect(linha.getByRole('cell', { name: '15', exact: true })).toBeVisible()
})

test('exit above the balance is refused by the API and the form keeps its data', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularEstoque(page, {
    movimentacao: { status: 400, json: { status: 400, error: 'Saldo Insuficiente', message: 'Saldo insuficiente em estoque. Saldo atual: 10, quantidade solicitada: 11' } },
  })
  await page.goto('/estoque')

  await page.getByRole('button', { name: 'Movimentar estoque de Capacete classe B' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Registrar movimentação' })
  await dialogo.getByRole('radio', { name: 'Saída (baixa)' }).check()
  await dialogo.getByLabel('Quantidade').fill('11')
  await dialogo.getByLabel('Motivo').fill('Tentativa indevida')
  await dialogo.getByRole('button', { name: 'Registrar' }).click()

  // A validação local já bloqueia a saída acima do saldo conhecido, sem chamar a API.
  await expect(dialogo.getByText('A saída não pode superar o saldo disponível (10).')).toBeVisible()
  await expect(dialogo.getByLabel('Quantidade')).toBeFocused()
  expect(chamadas).toHaveLength(0)

  // Com uma saída dentro do saldo conhecido, a recusa vem do servidor e a mensagem dele é exibida.
  await dialogo.getByLabel('Quantidade').fill('9')
  await dialogo.getByRole('button', { name: 'Registrar' }).click()
  await expect(dialogo.getByRole('alert')).toContainText('Saldo insuficiente em estoque. Saldo atual: 10, quantidade solicitada: 11')
  expect(chamadas).toHaveLength(1)
  await expect(dialogo.getByLabel('Quantidade')).toHaveValue('9')
  await expect(dialogo.getByLabel('Motivo')).toHaveValue('Tentativa indevida')
  await expect(page.getByRole('row', { name: /Capacete classe B/ }).getByRole('cell', { name: '10', exact: true })).toBeVisible()
})

test('zero quantity and empty reason are blocked locally without calling the API', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularEstoque(page)
  await page.goto('/estoque')

  await page.getByRole('button', { name: 'Movimentar estoque de Capacete classe B' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Registrar movimentação' })
  await dialogo.getByLabel('Quantidade').fill('0')
  await dialogo.getByRole('button', { name: 'Registrar' }).click()

  await expect(dialogo.getByText('Informe uma quantidade inteira maior que zero.')).toBeVisible()
  await expect(dialogo.getByText('Informe o motivo da movimentação.')).toBeVisible()
  await expect(dialogo.getByLabel('Quantidade')).toBeFocused()
  expect(chamadas).toHaveLength(0)
})

test('search filters by name or location and the keyboard reaches every action', async ({ page }) => {
  await iniciarSessao(page, gestora)
  await simularEstoque(page)
  await page.goto('/estoque')

  await page.getByRole('searchbox', { name: 'Nome ou localização' }).fill('almoxarifado')
  await page.getByRole('button', { name: 'Buscar' }).click()
  await expect(page.getByRole('row', { name: /Capacete classe B/ })).toBeVisible()
  await expect(page.getByRole('row', { name: /Luva nitrílica/ })).toHaveCount(0)
  await page.getByRole('searchbox', { name: 'Nome ou localização' }).fill('inexistente')
  await page.keyboard.press('Enter')
  await expect(page.getByRole('status').filter({ hasText: 'Nenhum EPI corresponde à busca' })).toBeVisible()
  await page.getByRole('button', { name: 'Limpar' }).click()
  await expect(page.getByRole('table', { name: 'Catálogo de EPIs em estoque' }).getByRole('row')).toHaveCount(3)

  await page.getByRole('button', { name: 'Detalhes e histórico de Luva nitrílica' }).focus()
  await page.keyboard.press('Enter')
  const dialogo = page.getByRole('dialog', { name: 'Luva nitrílica' })
  await expect(dialogo).toBeVisible()
  await expect(dialogo.getByText('Sem movimentações registradas')).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(dialogo).not.toBeVisible()
  await expect(page.getByRole('button', { name: 'Detalhes e histórico de Luva nitrílica' })).toBeFocused()
})

for (const width of [360, 768, 1344]) {
  test(`stock page has no horizontal overflow at ${width}px`, async ({ page }, testInfo) => {
    const errors: string[] = []
    page.on('pageerror', error => errors.push(error.message))
    await page.setViewportSize({ width, height: 960 })
    await iniciarSessao(page, gestora)
    await simularEstoque(page)
    await page.goto('/estoque')

    await expect(page.getByRole('table', { name: 'Catálogo de EPIs em estoque' })).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    await page.getByRole('button', { name: 'Movimentar estoque de Luva nitrílica' }).click()
    await expect(page.getByRole('dialog', { name: 'Registrar movimentação' })).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    await page.screenshot({ path: testInfo.outputPath(`epis-${width}.png`), fullPage: true })
    expect(errors).toEqual([])
  })
}
