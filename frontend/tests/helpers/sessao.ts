// Apoio aos testes Playwright: sessões e pessoas de exemplo, gravação da sessão no sessionStorage
// antes da página carregar e captura das chamadas à API.

import type { Page, Route } from '@playwright/test'

export type SessaoTeste = {
  token: string
  email: string
  nome: string
  perfil: 'GESTOR_SEGURANCA' | 'SUPERVISOR'
}

export const gestora: SessaoTeste = {
  token: 'token-gestora', email: 'gestora@safeplace.test', nome: 'Gestora Ana', perfil: 'GESTOR_SEGURANCA',
}

export const supervisor: SessaoTeste = {
  token: 'token-supervisor', email: 'supervisor@safeplace.test', nome: 'Supervisor Bruno', perfil: 'SUPERVISOR',
}

/** Registros no formato de `UsuarioResposta` do backend. */
export const pessoas = {
  gestora: { id: 1, nome: 'Gestora Ana', cpf: '52998224725', dataNascimento: '1980-01-15', email: 'gestora@safeplace.test', perfil: 'GESTOR_SEGURANCA', ativo: true },
  supervisora: { id: 2, nome: 'Joana Ribeiro', cpf: '11144477735', dataNascimento: '1988-04-20', email: 'joana.ribeiro@safeplace.test', perfil: 'SUPERVISOR', ativo: true },
  colaborador: { id: 3, nome: 'João da Silva', cpf: '12345678909', dataNascimento: '1992-06-18', email: null, perfil: null, ativo: true },
}

/**
 * Para o Gestor, o menu consulta os novos relatos em `GET /api/ocorrencias` em qualquer tela. Essa
 * consulta responde lista vazia por padrão, para que nenhum teste dependa de um backend real (um 401
 * encerraria a sessão). Testes que simulam ocorrências registram a própria rota depois e têm precedência.
 */
async function semNovosRelatos(page: Page) {
  await page.route(url => url.pathname === '/api/ocorrencias', route =>
    route.request().method() === 'GET' ? route.fulfill({ json: [] }) : route.fallback())
}

/** Grava a sessão antes de cada carregamento, como se o login já tivesse acontecido nesta aba. */
export async function iniciarSessao(page: Page, sessao: SessaoTeste = gestora) {
  await page.addInitScript(valor => {
    window.sessionStorage.setItem('safeplace.sessao', JSON.stringify(valor))
  }, sessao)
  await semNovosRelatos(page)
}

export type ChamadaApi = { url: URL; method: string; authorization: string | undefined }

type Lista = unknown[] | ((url: URL) => unknown[])

function resolver(lista: Lista | undefined, url: URL): unknown[] {
  return typeof lista === 'function' ? lista(url) : lista ?? []
}

/**
 * Simula as consultas `GET /api/usuarios` e `GET /api/usuarios/colaboradores`. Outros métodos
 * passam adiante (`fallback`) para os manipuladores registrados pelo próprio teste.
 */
export async function simularListagens(page: Page, dados: { usuarios?: Lista; colaboradores?: Lista } = {}) {
  await semNovosRelatos(page)
  const chamadas: ChamadaApi[] = []
  const atender = (lista: Lista | undefined) => (route: Route) => {
    const request = route.request()
    if (request.method() !== 'GET') return route.fallback()
    const url = new URL(request.url())
    chamadas.push({ url, method: request.method(), authorization: request.headers()['authorization'] })
    return route.fulfill({ json: resolver(lista, url) })
  }
  await page.route('**/api/usuarios', atender(dados.usuarios))
  await page.route('**/api/usuarios/colaboradores**', atender(dados.colaboradores))
  return chamadas
}

export async function sessaoGuardada(page: Page) {
  return page.evaluate(() => JSON.parse(window.sessionStorage.getItem('safeplace.sessao') ?? 'null'))
}
