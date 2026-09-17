export type PerfilComAcesso = 'GESTOR_SEGURANCA' | 'SUPERVISOR'

export type Sessao = {
  token: string
  email: string
  nome: string
  perfil: PerfilComAcesso
}

/**
 * A sessão fica em `sessionStorage`: sobrevive a recarregar a página, mas termina ao fechar a
 * aba. A issue #94 pede para não impor `localStorage` nem nova política de autenticação; a
 * validade real do token é decidida pelo backend (RNF03).
 */
export const CHAVE_SESSAO = 'safeplace.sessao'

export const rotuloDoPerfil: Record<PerfilComAcesso, string> = {
  GESTOR_SEGURANCA: 'Gestor de Segurança',
  SUPERVISOR: 'Supervisor',
}

export function ehPerfilComAcesso(valor: unknown): valor is PerfilComAcesso {
  return valor === 'GESTOR_SEGURANCA' || valor === 'SUPERVISOR'
}

/** Aceita a resposta de `POST /api/auth/login` ou o JSON guardado no navegador. */
export function lerSessaoDe(dados: unknown): Sessao {
  if (dados === null || typeof dados !== 'object') throw new Error('Sessão inválida')
  const { token, email, nome, perfil } = dados as Record<string, unknown>
  if (typeof token !== 'string' || !token.trim() || typeof email !== 'string'
    || typeof nome !== 'string' || !ehPerfilComAcesso(perfil)) {
    throw new Error('Sessão inválida')
  }
  return { token, email, nome, perfil }
}

function lerDoArmazenamento(): Sessao | null {
  try {
    const bruto = window.sessionStorage.getItem(CHAVE_SESSAO)
    return bruto ? lerSessaoDe(JSON.parse(bruto)) : null
  } catch {
    apagarSessao()
    return null
  }
}

// Fonte única em memória. O interceptor HTTP e o provedor React leem daqui, assim o token vai
// na primeira requisição mesmo antes de qualquer efeito do React rodar.
let sessaoEmMemoria: Sessao | null | undefined

export function sessaoAtual(): Sessao | null {
  if (sessaoEmMemoria === undefined) sessaoEmMemoria = lerDoArmazenamento()
  return sessaoEmMemoria
}

export function guardarSessao(sessao: Sessao) {
  sessaoEmMemoria = sessao
  try { window.sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao)) } catch { /* armazenamento indisponível: a sessão vive só em memória */ }
}

export function apagarSessao() {
  sessaoEmMemoria = null
  try { window.sessionStorage.removeItem(CHAVE_SESSAO) } catch { /* nada a apagar */ }
}
