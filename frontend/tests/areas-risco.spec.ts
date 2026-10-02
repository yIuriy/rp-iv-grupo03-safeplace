// Testes da aba de áreas de risco (RF05, UC03): filtro, consulta, matriz de perfis, cadastro e
// edição, com respostas HTTP simuladas no formato da API.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

const episMock = [
  { id: 1, nome: 'Capacete de segurança', numeroCa: '12345', quantidade: 20, estoqueMinimo: 5, status: 'DISPONIVEL', dataValidadeCa: '2028-01-01' },
  { id: 2, nome: 'Óculos de proteção', numeroCa: '67890', quantidade: 15, estoqueMinimo: 5, status: 'DISPONIVEL', dataValidadeCa: '2028-01-01' },
]

// Formato de `SetorResponse` do backend: o setor não tem código (issue #174).
const setoresMock = [
  { id: 1, nome: 'Galpão Principal', descricao: 'Área de manufatura e usinagem', localizacao: 'Bloco 1', status: 'ALTO' },
  { id: 2, nome: 'Doca de Carga', descricao: 'Movimentação e logística', localizacao: null, status: 'MEDIO' },
]

const area1 = {
  id: 1,
  codigo: 'SET-01',
  nome: 'Linha de Solda',
  descricao: 'Setor de soldagem pesada e montagem',
  nivelPerigo: 'ALTO',
  setorId: 1,
  agentesRisco: ['Fumos metálicos', 'Ruído elevado'],
  limites: 'Piso 1 - Demarcação amarela',
  episObrigatorios: [
    { id: 1, nome: 'Capacete de segurança', numeroCa: '12345', status: 'DISPONIVEL' },
  ],
}

const area2 = {
  id: 2,
  codigo: 'SET-02',
  nome: 'Doca Externa',
  descricao: 'Área de manobra de caminhões',
  nivelPerigo: 'MEDIO',
  setorId: 2,
  agentesRisco: ['Tráfego de veículos'],
  limites: 'Pátio externo sul',
  episObrigatorios: [
    { id: 2, nome: 'Óculos de proteção', numeroCa: '67890', status: 'DISPONIVEL' },
  ],
}

type Chamada = { method: string; url: string; body: unknown }

async function simularAreasRisco(page: Page, opcoes: { criacao?: unknown; atualizacao?: unknown } = {}) {
  const chamadas: Chamada[] = []
  let areas = [area1, area2]

  await page.route('**/api/setores', route => route.fulfill({ json: setoresMock }))
  await page.route('**/api/epis', route => route.fulfill({ json: episMock }))

  await page.route('**/api/areas-risco', route => {
    const req = route.request()
    if (req.method() === 'GET') {
      const url = new URL(req.url())
      const perigo = url.searchParams.get('nivelPerigo')
      const retorno = perigo ? areas.filter(a => a.nivelPerigo === perigo) : areas
      return route.fulfill({ json: retorno })
    }
    if (req.method() === 'POST') {
      chamadas.push({ method: req.method(), url: req.url(), body: req.postDataJSON() })
      const nova = (opcoes.criacao as typeof area1) ?? {
        id: 3,
        codigo: 'SET-03',
        nome: 'Almoxarifado Químico',
        descricao: 'Armazenamento de solventes',
        nivelPerigo: 'CRITICO',
        setorId: 1,
        agentesRisco: ['Vapores orgânicos'],
        limites: 'Sala blindada',
        episObrigatorios: [{ id: 2, nome: 'Óculos de proteção', numeroCa: '67890', status: 'DISPONIVEL' }],
      }
      areas = [...areas, nova]
      return route.fulfill({ status: 201, json: nova })
    }
    return route.fallback()
  })

  await page.route('**/api/areas-risco/*', route => {
    const req = route.request()
    if (req.method() === 'PUT') {
      chamadas.push({ method: req.method(), url: req.url(), body: req.postDataJSON() })
      const body = req.postDataJSON() as Record<string, unknown>
      const atualizada = {
        ...area1,
        nome: String(body.nome ?? area1.nome),
        nivelPerigo: String(body.nivelPerigo ?? area1.nivelPerigo),
      }
      areas = areas.map(a => (a.id === 1 ? (atualizada as typeof area1) : a))
      return route.fulfill({ json: atualizada })
    }
    return route.fallback()
  })

  return chamadas
}

test.describe('Módulo Áreas de Risco (RF05, UC03, RNF11)', () => {
  test('lista áreas, filtra por vocabulário de risco #112 e busca textualmente', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularAreasRisco(page)
    await page.goto('/areas-risco')

    const card1 = page.locator('article', { hasText: 'SET-01 - Linha de Solda' })
    const card2 = page.locator('article', { hasText: 'SET-02 - Doca Externa' })

    await expect(card1).toBeVisible()
    await expect(card2).toBeVisible()
    await expect(card1.locator('.sp-badge')).toHaveText('Alto')
    await expect(card2.locator('.sp-badge')).toHaveText('Médio')

    // Filtro por perigo "Alto"
    await page.getByRole('search', { name: 'Buscar e filtrar áreas de risco' }).getByLabel('Grau de perigo').selectOption('ALTO')
    await expect(card1).toBeVisible()
    await expect(card2).not.toBeVisible()

    // Limpar filtros
    await page.getByRole('button', { name: 'Limpar' }).click()
    await expect(card2).toBeVisible()
  })

  test('consulta detalhes da área e apresenta vínculos de proteção sem confundir com RF12', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularAreasRisco(page)
    await page.goto('/areas-risco')

    const card = page.locator('article', { hasText: 'SET-01 - Linha de Solda' })
    await card.getByRole('button', { name: /Consultar área/ }).click()

    const dialog = page.getByRole('dialog', { name: 'SET-01 - Linha de Solda' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByText('Fumos metálicos, Ruído elevado')).toBeVisible()
    await expect(dialog.getByText('Capacete de segurança (CA 12345)')).toBeVisible()
    await expect(dialog.getByText(/Não se trata do recálculo automático de vínculos/)).toBeVisible()

    // Fecha via Escape
    await page.keyboard.press('Escape')
    await expect(dialog).not.toBeVisible()
  })

  test('matriz RBAC: Supervisor tem acesso de leitura sem botões de cadastro ou edição', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularAreasRisco(page)
    await page.goto('/areas-risco')

    // Botão de cadastro não existe para supervisor
    await expect(page.getByRole('button', { name: 'Cadastrar área' })).toHaveCount(0)

    // Consulta detalhes da área
    const card = page.locator('article', { hasText: 'SET-01 - Linha de Solda' })
    await card.getByRole('button', { name: /Consultar área/ }).click()

    const dialog = page.getByRole('dialog', { name: 'SET-01 - Linha de Solda' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByRole('button', { name: 'Editar área' })).toHaveCount(0)
    await dialog.getByRole('button', { name: 'Fechar consulta' }).click()
    await expect(dialog).not.toBeVisible()
  })

  test('Gestor cadastra nova área com validação e persistência na API real', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const chamadas = await simularAreasRisco(page)
    await page.goto('/areas-risco')

    await page.getByRole('button', { name: 'Cadastrar área' }).click()
    const dialog = page.getByRole('dialog', { name: 'Cadastrar área de risco' })
    await expect(dialog).toBeVisible()

    // Validação client-side bloqueando envio vazio
    await dialog.getByRole('button', { name: 'Cadastrar área' }).click()
    await expect(dialog.getByText('O código da área é obrigatório.')).toBeVisible()

    // Preenche dados válidos
    await dialog.getByLabel(/Código da área/).fill('SET-03')
    await dialog.getByLabel(/Nome da área/).fill('Almoxarifado Químico')
    await dialog.getByLabel(/Grau de perigo/).selectOption('CRITICO')
    await dialog.getByLabel('Óculos de proteção (CA 67890)').check()

    await dialog.getByRole('button', { name: 'Cadastrar área' }).click()
    await expect(dialog).not.toBeVisible()

    // Verifica chamada POST
    expect(chamadas).toHaveLength(1)
    expect(chamadas[0].method).toBe('POST')
    expect(chamadas[0].body).toEqual({
      codigo: 'SET-03',
      nome: 'Almoxarifado Químico',
      nivelPerigo: 'CRITICO',
      agentesRisco: [],
      episObrigatoriosIds: [2],
    })

    // Sucesso exibido
    await expect(page.getByText('Área de risco SET-03 cadastrada com sucesso.')).toBeVisible()
    await expect(page.getByText('SET-03 - Almoxarifado Químico')).toBeVisible()
  })

  test('Gestor edita área de risco existente justificando alteração', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const chamadas = await simularAreasRisco(page)
    await page.goto('/areas-risco')

    const card = page.locator('article', { hasText: 'SET-01 - Linha de Solda' })
    await card.getByRole('button', { name: /Consultar área/ }).click()

    await page.getByRole('button', { name: 'Editar área' }).click()
    const dialog = page.getByRole('dialog', { name: 'Editar área: SET-01' })
    await expect(dialog).toBeVisible()

    // Código deve estar desabilitado
    await expect(dialog.getByLabel(/Código da área/)).toBeDisabled()

    await dialog.getByLabel(/Nome da área/).fill('Linha de Solda e Montagem')
    await dialog.getByLabel(/Justificativa da alteração/).fill('Revisão semestral do mapa de riscos')

    await dialog.getByRole('button', { name: 'Salvar alterações' }).click()
    await expect(dialog).not.toBeVisible()

    expect(chamadas).toHaveLength(1)
    expect(chamadas[0].method).toBe('PUT')
    expect(chamadas[0].url).toContain('/api/areas-risco/1')

    await expect(page.getByText('Área de risco SET-01 atualizada com sucesso.')).toBeVisible()
  })
})
