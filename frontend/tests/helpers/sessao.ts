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
  colaborador: { id: 3, nome: 'João da Silva', cpf: '12345678909', dataNascimento: '1992-06-18', email: null, perfil: 'COLABORADOR', ativo: true },
}

/** Grava a sessão antes de cada carregamento, como se o login já tivesse acontecido nesta aba. */
export async function iniciarSessao(page: Page, sessao: SessaoTeste = gestora) {
  await page.addInitScript(valor => {
    window.sessionStorage.setItem('safeplace.sessao', JSON.stringify(valor))
  }, sessao)
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
