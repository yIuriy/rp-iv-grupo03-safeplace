import { http } from '../../shared/api/https'
import { statusDoErro } from '../../shared/api/erros'
import { sessaoAtual } from './sessao'

const ouvintesDeExpiracao = new Set<() => void>()
let instalados = false

/** Avisa quando o backend rejeita o token de uma sessão ativa. Devolve a função para cancelar. */
export function aoExpirarSessao(ouvinte: () => void): () => void {
  ouvintesDeExpiracao.add(ouvinte)
  return () => { ouvintesDeExpiracao.delete(ouvinte) }
}

function ehRequisicaoDeLogin(erro: unknown): boolean {
  const url = (erro as { config?: { url?: unknown } } | null)?.config?.url
  return typeof url === 'string' && url.endsWith('/auth/login')
}

/**
 * Anexa o token a toda chamada e detecta 401 fora do login, que cobre token expirado ou revogado
 * (RNF03) sem fixar prazo no navegador. Idempotente: instala uma única vez no cliente compartilhado.
 */
export function instalarInterceptadores() {
  if (instalados) return
  instalados = true
  http.interceptors.request.use(config => {
    const token = sessaoAtual()?.token
    if (token && !config.headers.has('Authorization')) config.headers.set('Authorization', `Bearer ${token}`)
    return config
  })
  http.interceptors.response.use(undefined, erro => {
    if (sessaoAtual() && statusDoErro(erro) === 401 && !ehRequisicaoDeLogin(erro)) {
      ouvintesDeExpiracao.forEach(ouvinte => ouvinte())
    }
    return Promise.reject(erro)
  })
}
