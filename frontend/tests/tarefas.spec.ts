import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

const tarefa1 = {
  id: 1,
  descricao: 'Corte a quente e oxicorte de tubulação',
  nivelPerigo: 'ALTO',
  classificada: true,
  dataClassificacao: '2026-09-20T14:30:00',
  responsavelClassificacaoNome: 'Gestora Demonstração',
}

const tarefa2 = {
  id: 2,
  descricao: 'Inspeção visual de esteira transportadora',
  nivelPerigo: null,
  classificada: false,
  dataClassificacao: null,
}

type Chamada = { method: string; url: string; body: unknown }

async function simularTarefas(page: Page, opcoes: { criacao?: unknown; classificacao?: unknown } = {}) {
  const chamadas: Chamada[] = []
  let tarefas = [tarefa1, tarefa2]

  await page.route('**/api/tarefas', route => {
    const req = route.request()
    if (req.method() === 'GET') {
      const url = new URL(req.url())
      const perigo = url.searchParams.get('nivelPerigo')
      const retorno = perigo ? tarefas.filter(t => t.nivelPerigo === perigo) : tarefas
      return route.fulfill({ json: retorno })
    }
    if (req.method() === 'POST') {
      chamadas.push({ method: req.method(), url: req.url(), body: req.postDataJSON() })
      const nova = (opcoes.criacao as typeof tarefa1) ?? {
        id: 3,
        descricao: 'Trabalho em altura com andaime fachadeiro',
        nivelPerigo: null,
        classificada: false,
        dataClassificacao: null,
      }
      tarefas = [...tarefas, nova]
      return route.fulfill({ status: 201, json: nova })
    }
    return route.fallback()
  })

  await page.route('**/api/tarefas/*/classificacao', route => {
    const req = route.request()
    if (req.method() === 'PATCH') {
      chamadas.push({ method: req.method(), url: req.url(), body: req.postDataJSON() })
      const body = req.postDataJSON() as { nivelPerigo: string }
      const atualizada = {
        ...tarefa2,
        nivelPerigo: body.nivelPerigo,
        classificada: true,
        dataClassificacao: '2026-09-24T18:00:00',
        responsavelClassificacaoNome: 'Gestora Demonstração',
      }
      tarefas = tarefas.map(t => (t.id === 2 ? atualizada : t))
      return route.fulfill({ json: atualizada })
    }
    return route.fallback()
  })

  return chamadas
}

test.describe('Módulo Classificação de Tarefas (RF06, UC11, RNF11)', () => {
  test('lista tarefas, exibe badges textuais de risco e filtra por nível', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularTarefas(page)
    await page.goto('/tarefas')

    const tabela = page.getByRole('table', { name: 'Matriz de tarefas e classificação de periculosidade' })
    await expect(tabela.getByText('Corte a quente e oxicorte de tubulação')).toBeVisible()
    await expect(tabela.getByText('Inspeção visual de esteira transportadora')).toBeVisible()
    await expect(tabela.getByText('Alto', { exact: true })).toBeVisible()
    await expect(tabela.getByText('Pendente de avaliação')).toBeVisible()
    await expect(
      tabela.getByRole('row').filter({ hasText: 'Corte a quente e oxicorte de tubulação' }).getByText('por Gestora Demonstração')
    ).toBeVisible()

    // Filtro por ALTO
    await page.getByLabel('Grau de perigo').selectOption('ALTO')
    await expect(tabela.getByText('Corte a quente e oxicorte de tubulação')).toBeVisible()
    await expect(tabela.getByText('Inspeção visual de esteira transportadora')).not.toBeVisible()

    // Limpar filtros
    await page.getByRole('button', { name: 'Limpar' }).click()
    await expect(tabela.getByText('Inspeção visual de esteira transportadora')).toBeVisible()
  })

  test('matriz RBAC: Supervisor visualiza a matriz sem permissão de cadastro ou classificação', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularTarefas(page)
    await page.goto('/tarefas')

    // Botão de cadastro não existe para supervisor
    await expect(page.getByRole('button', { name: 'Nova tarefa' })).toHaveCount(0)

    // Supervisor só consulta as exigências (US06, critério 4): sem edição, classificação ou histórico
    const tabela = page.getByRole('table', { name: 'Matriz de tarefas e classificação de periculosidade' })
    await expect(tabela.getByRole('button', { name: /^Ver exigências da tarefa/ })).toHaveCount(2)
    await expect(tabela.getByRole('button', { name: /Classificar|Reavaliar|Editar|Histórico/ })).toHaveCount(0)
  })

  test('Gestor cadastra nova tarefa com validação client-side e persistência', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const chamadas = await simularTarefas(page)
    await page.goto('/tarefas')

    await page.getByRole('button', { name: 'Nova tarefa' }).click()
    const dialog = page.getByRole('dialog', { name: 'Cadastrar nova tarefa' })
    await expect(dialog).toBeVisible()

    // Tentativa com campo vazio
    await dialog.getByRole('button', { name: 'Cadastrar tarefa' }).click()
    await expect(dialog.getByText('A descrição da atividade é obrigatória.')).toBeVisible()

    // Preenche descrição
    await dialog.getByLabel(/Descrição da atividade operacional/).fill('Trabalho em altura com andaime fachadeiro')
    await dialog.getByRole('button', { name: 'Cadastrar tarefa' }).click()
    await expect(dialog).not.toBeVisible()

    expect(chamadas).toHaveLength(1)
    expect(chamadas[0].method).toBe('POST')
    expect(chamadas[0].body).toEqual({
      descricao: 'Trabalho em altura com andaime fachadeiro',
      setoresIds: [],
    })

    await expect(page.getByText('Tarefa cadastrada com sucesso: "Trabalho em altura com andaime fachadeiro".')).toBeVisible()
  })

  test('Gestor classifica tarefa pendente via PATCH de periculosidade', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const chamadas = await simularTarefas(page)
    await page.goto('/tarefas')

    const tabela = page.getByRole('table', { name: 'Matriz de tarefas e classificação de periculosidade' })
    const linha = tabela.getByRole('row', { hasText: 'Inspeção visual de esteira transportadora' })
    await linha.getByRole('button', { name: /Classificar/ }).click()

    const dialog = page.getByRole('dialog', { name: 'Classificar tarefa' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByText('Inspeção visual de esteira transportadora')).toBeVisible()

    // #112: vocabulário único NivelPerigo, sem os termos leve, moderado ou grave de UC11
    await expect(dialog.getByLabel('Grau de periculosidade *').locator('option')).toHaveText([
      'Baixo',
      'Médio',
      'Alto',
      'Crítico',
    ])

    await dialog.getByLabel('Grau de periculosidade *').selectOption('CRITICO')
    await dialog.getByRole('button', { name: 'Salvar classificação' }).click()
    await expect(dialog).not.toBeVisible()

    expect(chamadas).toHaveLength(1)
    expect(chamadas[0].method).toBe('PATCH')
    expect(chamadas[0].url).toContain('/api/tarefas/2/classificacao')
    expect(chamadas[0].body).toEqual({ nivelPerigo: 'CRITICO' })

    await expect(page.getByText('Grau de periculosidade atualizado para Crítico.')).toBeVisible()
    await expect(
      tabela.getByRole('row').filter({ hasText: 'Inspeção visual de esteira transportadora' }).getByText('por Gestora Demonstração')
    ).toBeVisible()
  })

  test('tarefa classificada antes do registro de autoria indica responsável não registrado', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await page.route('**/api/tarefas', route =>
      route.fulfill({ json: [{ ...tarefa1, responsavelClassificacaoNome: null }] })
    )
    await page.goto('/tarefas')

    const tabela = page.getByRole('table', { name: 'Matriz de tarefas e classificação de periculosidade' })
    await expect(tabela.getByText('Responsável não registrado')).toBeVisible()
  })
})
