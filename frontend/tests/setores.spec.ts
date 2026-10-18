// Testes da aba "Setores físicos" (UC03, issue #174): consulta, cadastro pelo Gestor, validação,
// erros da API, matriz de perfis, teclado e larguras. Só as respostas HTTP são simuladas.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

// Formato de `SetorResponse` e `AreaRiscoResponse` do backend.
const galpao = { id: 1, nome: 'Galpão Principal', descricao: 'Usinagem e montagem', localizacao: 'Bloco 1', status: 'ALTO' }
const doca = { id: 2, nome: 'Doca de Carga', descricao: null, localizacao: null, status: 'MEDIO' }
const areaSolda = {
  id: 1, codigo: 'AR-01', nome: 'Linha de Solda', descricao: null, nivelPerigo: 'ALTO', setorId: 1,
  agentesRisco: ['Fumos metálicos'], limites: 'Piso 1', episObrigatorios: [{ id: 1, nome: 'Capacete', numeroCa: '12345', status: 'DISPONIVEL' }],
}

type Simulacao = {
  setores?: unknown[]
  falharPrimeiraConsulta?: boolean
  respostaCadastro?: { status: number; json: unknown }
}

async function simular(page: Page, opcoes: Simulacao = {}) {
  let setores = opcoes.setores ?? [galpao, doca]
  let consultas = 0
  const cadastros: { body: unknown; authorization: string | undefined }[] = []

  await page.route('**/api/areas-risco', route => route.fulfill({ json: [areaSolda] }))
  await page.route('**/api/epis', route => route.fulfill({ json: [] }))
  await page.route('**/api/setores', route => {
    const req = route.request()
    if (req.method() === 'GET') {
      consultas += 1
      if (opcoes.falharPrimeiraConsulta && consultas === 1) return route.fulfill({ status: 500, json: {} })
      return route.fulfill({ json: setores })
    }
    cadastros.push({ body: req.postDataJSON(), authorization: req.headers()['authorization'] })
    if (opcoes.respostaCadastro) return route.fulfill(opcoes.respostaCadastro)
    const novo = { id: 3, descricao: null, localizacao: null, ...(req.postDataJSON() as object) }
    setores = [...setores, novo]
    return route.fulfill({ status: 201, json: novo })
  })
  return { cadastros, consultas: () => consultas }
}

async function abrirSetores(page: Page) {
  await page.goto('/areas-risco')
  await page.getByRole('tab', { name: 'Setores físicos' }).click()
  return page.getByRole('tabpanel', { name: 'Setores físicos' })
}

test.describe('Setores físicos (UC03, issue #174)', () => {
  test('lista setores com grau em texto e áreas vinculadas', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page)
    const painel = await abrirSetores(page)

    const tabela = painel.getByRole('table', { name: 'Setores físicos cadastrados' })
    const linhaGalpao = tabela.getByRole('row', { name: /Galpão Principal/ })
    await expect(linhaGalpao).toContainText('Bloco 1')
    await expect(linhaGalpao).toContainText('Alto')
    await expect(linhaGalpao).toContainText('1 área')
    const linhaDoca = tabela.getByRole('row', { name: /Doca de Carga/ })
    await expect(linhaDoca).toContainText('Não informada')
    await expect(linhaDoca).toContainText('Médio')
    await expect(linhaDoca).toContainText('0 áreas')
  })

  test('Gestor cadastra setor e ele aparece no formulário de área sem recarregar a página', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const api = await simular(page)
    const painel = await abrirSetores(page)

    await painel.getByRole('button', { name: 'Cadastrar setor' }).click()
    const dialogo = page.getByRole('dialog', { name: 'Cadastrar setor físico' })
    await expect(dialogo.getByLabel('Nome do setor')).toBeFocused()
    await dialogo.getByLabel('Nome do setor').fill('  Caldeiraria  ')
    await dialogo.getByLabel('Grau de perigo').selectOption('CRITICO')
    await dialogo.getByLabel('Localização na planta (opcional)').fill('Bloco 3')
    await dialogo.getByRole('button', { name: 'Cadastrar setor' }).click()

    await expect(dialogo).not.toBeVisible()
    expect(api.cadastros).toHaveLength(1)
    // Só os campos de `CriarSetorRequest`; descrição vazia não é enviada.
    expect(api.cadastros[0].body).toEqual({ nome: 'Caldeiraria', status: 'CRITICO', localizacao: 'Bloco 3' })
    expect(api.cadastros[0].authorization).toBe(`Bearer ${gestora.token}`)
    await expect(page.getByText('Setor Caldeiraria cadastrado. Ele já pode ser escolhido no cadastro de áreas de risco.')).toBeVisible()
    await expect(painel.getByRole('row', { name: /Caldeiraria/ })).toContainText('Crítico')
    expect(api.consultas()).toBe(2)

    await page.getByRole('tab', { name: 'Áreas de risco' }).click()
    await page.getByRole('button', { name: 'Cadastrar área' }).click()
    const formularioArea = page.getByRole('dialog', { name: 'Cadastrar área de risco' })
    await expect(formularioArea.getByLabel('Setor físico (opcional)').locator('option', { hasText: 'Caldeiraria (Bloco 3)' }))
      .toHaveCount(1)
  })

  test('validação local bloqueia o envio, foca o primeiro erro e mantém os dados', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const api = await simular(page)
    const painel = await abrirSetores(page)
    await painel.getByRole('button', { name: 'Cadastrar setor' }).click()
    const dialogo = page.getByRole('dialog', { name: 'Cadastrar setor físico' })

    await dialogo.getByRole('button', { name: 'Cadastrar setor' }).click()
    await expect(dialogo.getByText('Informe o nome do setor.')).toBeVisible()
    await expect(dialogo.getByText('Selecione o grau de perigo do setor.')).toBeVisible()
    await expect(dialogo.getByLabel('Nome do setor')).toBeFocused()
    await expect(dialogo.getByLabel('Nome do setor')).toHaveAttribute('aria-invalid', 'true')
    await expect(dialogo.getByRole('alert').filter({ hasText: 'Confira os campos destacados' })).toBeVisible()

    await dialogo.getByLabel('Nome do setor').fill('Pintura')
    await dialogo.getByRole('button', { name: 'Cadastrar setor' }).click()
    await expect(dialogo.getByLabel('Grau de perigo')).toBeFocused()
    await expect(dialogo.getByLabel('Nome do setor')).toHaveValue('Pintura')
    expect(api.cadastros).toHaveLength(0)
  })

  test('recusa da API mostra a mensagem do servidor, mantém os dados e não confirma sucesso', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const api = await simular(page, {
      respostaCadastro: { status: 400, json: { status: 400, message: 'nome: Nome do setor é obrigatório.' } },
    })
    const painel = await abrirSetores(page)
    await painel.getByRole('button', { name: 'Cadastrar setor' }).click()
    const dialogo = page.getByRole('dialog', { name: 'Cadastrar setor físico' })
    await dialogo.getByLabel('Nome do setor').fill('Pintura')
    await dialogo.getByLabel('Grau de perigo').selectOption('BAIXO')
    await dialogo.getByRole('button', { name: 'Cadastrar setor' }).click()

    await expect(dialogo.getByRole('alert')).toContainText('nome: Nome do setor é obrigatório.')
    await expect(dialogo.getByLabel('Nome do setor')).toHaveValue('Pintura')
    await expect(page.getByText(/Setor Pintura cadastrado/)).toHaveCount(0)
    expect(api.cadastros).toHaveLength(1)
    expect(api.consultas()).toBe(1)
  })

  test('Supervisor consulta os setores sem ação de cadastro', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simular(page)
    const painel = await abrirSetores(page)

    await expect(painel.getByRole('row', { name: /Galpão Principal/ })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Cadastrar setor' })).toHaveCount(0)
  })

  test('lista vazia orienta o Gestor a cadastrar o primeiro setor', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page, { setores: [] })
    const painel = await abrirSetores(page)
    await expect(painel.getByText('Cadastre o primeiro setor para vincular as áreas de risco à planta.')).toBeVisible()
  })

  // Contexto novo, sem cópia offline: a falha aparece como erro, e não como dados locais (RNF08).
  test('falha de carga sem cópia local permite nova tentativa', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page, { falharPrimeiraConsulta: true })
    const painel = await abrirSetores(page)
    await expect(painel.getByRole('alert')).toContainText('Não foi possível carregar os setores.')
    await painel.getByRole('button', { name: 'Tentar novamente' }).click()
    await expect(painel.getByRole('row', { name: /Galpão Principal/ })).toBeVisible()
  })

  test('abas respondem às setas do teclado', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simular(page)
    await page.goto('/areas-risco')

    const abaAreas = page.getByRole('tab', { name: 'Áreas de risco' })
    await expect(abaAreas).toHaveAttribute('aria-selected', 'true')
    await abaAreas.focus()
    await page.keyboard.press('ArrowRight')
    const abaSetores = page.getByRole('tab', { name: 'Setores físicos' })
    await expect(abaSetores).toBeFocused()
    await expect(abaSetores).toHaveAttribute('aria-selected', 'true')
    await expect(page.getByRole('tabpanel', { name: 'Setores físicos' }).getByRole('table')).toBeVisible()
  })

  for (const width of [360, 768, 1344]) {
    test(`aba de setores e diálogo cabem em ${width} px sem rolagem horizontal`, async ({ page }) => {
      await page.setViewportSize({ width, height: 900 })
      await iniciarSessao(page, gestora)
      await simular(page)
      const painel = await abrirSetores(page)
      await expect(painel.getByRole('table')).toBeVisible()
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

      await painel.getByRole('button', { name: 'Cadastrar setor' }).click()
      await expect(page.getByRole('dialog', { name: 'Cadastrar setor físico' })).toBeVisible()
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    })
  }
})
