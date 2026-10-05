// Testes Playwright da tela de perfil: atalho no rodapé do menu lateral, dados da pessoa logada
// vindos de GET /api/usuarios/me, CPF oculto, falha com nova tentativa, atalho no menu em diálogo
// das telas menores e ações recentes de GET /api/usuarios/me/historico (RNF05).

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, pessoas, supervisor } from './helpers/sessao'

type ChamadaPerfil = { authorization: string | undefined }

type RespostaHistorico = Array<Record<string, unknown>> | 'erro'

/** Ações recentes: lista vazia por padrão, para os testes do perfil não dependerem de um backend real. */
async function simularHistorico(page: Page, respostas: RespostaHistorico[] = [[]]) {
  const limites: Array<string | null> = []
  await page.route(url => url.pathname === '/api/usuarios/me/historico', route => {
    limites.push(new URL(route.request().url()).searchParams.get('limite'))
    const resposta = respostas[Math.min(limites.length - 1, respostas.length - 1)]
    return resposta === 'erro'
      ? route.fulfill({ status: 500, json: { message: 'Falha interna' } })
      : route.fulfill({ json: resposta })
  })
  return limites
}

async function simularPerfil(page: Page, respostas: Array<Record<string, unknown> | 'erro'>, historico?: RespostaHistorico[]) {
  await simularHistorico(page, historico)
  const chamadas: ChamadaPerfil[] = []
  await page.route(url => url.pathname === '/api/usuarios/me', route => {
    chamadas.push({ authorization: route.request().headers().authorization })
    const resposta = respostas[Math.min(chamadas.length - 1, respostas.length - 1)]
    return resposta === 'erro'
      ? route.fulfill({ status: 500, json: { message: 'Falha interna' } })
      : route.fulfill({ json: resposta })
  })
  return chamadas
}

const supervisoraLogada = {
  ...pessoas.supervisora, email: supervisor.email, nome: supervisor.nome,
  criadoEm: '2026-09-20T10:30:00', senha: 'never-render-this-hash',
}

test('Supervisor abre o perfil pelo atalho do rodapé do menu e consulta os próprios dados', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularPerfil(page, [supervisoraLogada])
  await page.route(url => url.pathname === '/api/usuarios/colaboradores', route => route.fulfill({ json: [] }))
  await page.goto('/usuarios')

  const atalho = page.getByRole('complementary').getByRole('link', { name: `Meu perfil: ${supervisor.nome} Supervisor` })
  await expect(atalho).toBeVisible()
  await expect(atalho).toContainText('SB')
  await atalho.click()

  await expect(page).toHaveURL(/\/perfil$/)
  await expect(page).toHaveTitle('Meu perfil | SafePlace')
  await expect(atalho).toHaveAttribute('aria-current', 'page')
  await expect(page.getByRole('heading', { level: 1, name: 'Meu perfil' })).toBeVisible()

  const dados = page.getByRole('main')
  await expect(dados.getByText(supervisor.nome, { exact: true })).toBeVisible()
  await expect(dados.getByText('Supervisor', { exact: true })).toBeVisible()
  await expect(dados.getByText(supervisor.email, { exact: true })).toBeVisible()
  await expect(dados.getByText('20/04/1988', { exact: true })).toBeVisible()
  await expect(dados.getByText('20/09/2026', { exact: true })).toBeVisible()
  await expect(dados.getByText('•••.444.777-••')).toBeVisible()
  await expect(page.getByText('111.444.777-35')).toHaveCount(0)
  await dados.getByRole('button', { name: `Exibir CPF de ${supervisor.nome}` }).click()
  await expect(dados.getByText('111.444.777-35')).toBeVisible()
  await expect(page.getByText('never-render-this')).toHaveCount(0)
  await expect(dados.getByRole('button', { name: /editar|salvar/i })).toHaveCount(0)

  expect(chamadas).toEqual([{ authorization: `Bearer ${supervisor.token}` }])
})

test('Gestor vê o perfil de Gestor de Segurança', async ({ page }) => {
  await iniciarSessao(page, gestora)
  await simularPerfil(page, [{ ...pessoas.gestora, criadoEm: null }])
  await page.goto('/perfil')

  const dados = page.getByRole('main')
  await expect(dados.getByText('Gestor de Segurança', { exact: true })).toBeVisible()
  await expect(dados.getByText('15/01/1980', { exact: true })).toBeVisible()
  await expect(dados.getByText('Não informado', { exact: true })).toBeVisible()
})

test('falha ao carregar o perfil mostra aviso e permite tentar de novo', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularPerfil(page, ['erro', supervisoraLogada])
  await page.goto('/perfil')

  await expect(page.getByRole('alert')).toContainText('Não foi possível carregar o seu perfil.')
  await page.getByRole('button', { name: 'Tentar novamente' }).click()
  await expect(page.getByRole('main').getByText(supervisor.email, { exact: true })).toBeVisible()
  expect(chamadas).toHaveLength(2)
})

test('em telas menores o atalho do perfil fica no menu em diálogo', async ({ page }) => {
  await page.setViewportSize({ width: 768, height: 1024 })
  await iniciarSessao(page, supervisor)
  await simularPerfil(page, [supervisoraLogada])
  await page.route(url => url.pathname === '/api/usuarios/colaboradores', route => route.fulfill({ json: [] }))
  await page.goto('/usuarios')

  await page.getByRole('button', { name: 'Menu' }).click()
  const menu = page.getByRole('dialog', { name: 'Navegação' })
  await menu.getByRole('link', { name: `Meu perfil: ${supervisor.nome} Supervisor` }).click()

  await expect(menu).toBeHidden()
  await expect(page).toHaveURL(/\/perfil$/)
  await expect(page.getByRole('main').getByText(supervisor.email, { exact: true })).toBeVisible()
})

const acoes = [
  { id: 31, tipoOperacao: 'CADASTRO', dataHora: '2026-10-05T20:53:27.1', entidade: 'Inspecao', entidadeId: 5,
    descricao: 'Inspeção de 03/10/2026 na área AR-03 - Corredor de estoque: 2 itens verificados, 0 não conformidades.' },
  { id: 20, tipoOperacao: 'ALTERACAO', dataHora: '2026-10-04T09:10:00', entidade: 'Epi', entidadeId: 3, descricao: null },
  { id: 12, tipoOperacao: 'REMOCAO', dataHora: '2026-10-01T08:00:00', entidade: 'NovoRegistro', entidadeId: null, descricao: 'Retirado' },
]

test.describe('Ações recentes no perfil (RNF05)', () => {
  test('Gestor vê as próprias ações com data, operação, registro e descrição', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularPerfil(page, [{ ...pessoas.gestora, criadoEm: null }])
    const limites = await simularHistorico(page, [acoes])
    await page.goto('/perfil')

    const secao = page.getByRole('region', { name: 'Minhas ações recentes' })
    await expect(secao.getByRole('heading', { level: 2, name: 'Minhas ações recentes' })).toBeVisible()
    const linhas = secao.getByRole('row')
    await expect(linhas).toHaveCount(4)
    await expect(linhas.nth(1)).toContainText('05/10/2026 20:53')
    await expect(linhas.nth(1)).toContainText('Cadastro')
    await expect(linhas.nth(1)).toContainText('Inspeção 5')
    await expect(linhas.nth(1)).toContainText('Corredor de estoque')
    await expect(linhas.nth(2)).toContainText('Alteração')
    await expect(linhas.nth(2)).toContainText('EPI 3')
    await expect(linhas.nth(2)).toContainText('Sem descrição')
    await expect(linhas.nth(3)).toContainText('Remoção')
    await expect(linhas.nth(3)).toContainText('NovoRegistro')
    expect(limites).toEqual(['20'])
  })

  test('sem ações registradas, a seção explica o que aparecerá', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularPerfil(page, [supervisoraLogada], [[]])
    await page.goto('/perfil')

    await expect(page.getByRole('heading', { name: 'Nenhuma ação registrada' })).toBeVisible()
  })

  test('falha no histórico não esconde os dados do perfil e permite tentar de novo', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularPerfil(page, [supervisoraLogada], ['erro', acoes])
    await page.goto('/perfil')

    await expect(page.getByRole('main').getByText(supervisor.email, { exact: true })).toBeVisible()
    await expect(page.getByRole('alert')).toContainText('Não foi possível carregar as suas ações.')
    await page.getByRole('button', { name: 'Tentar novamente' }).click()
    await expect(page.getByRole('region', { name: 'Minhas ações recentes' }).getByText('Inspeção 5')).toBeVisible()
  })
})
