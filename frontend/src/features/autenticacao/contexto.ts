import { createContext, useContext } from 'react'
import type { Sessao } from './sessao'

export type MotivoSaida = 'expirada' | 'manual'

export type Autenticacao = {
  sessao: Sessao | null
  motivoSaida: MotivoSaida | null
  entrar: (email: string, senha: string) => Promise<Sessao>
  sair: () => void
}

export const AutenticacaoContexto = createContext<Autenticacao | null>(null)

export function useAutenticacao(): Autenticacao {
  const valor = useContext(AutenticacaoContexto)
  if (!valor) throw new Error('useAutenticacao precisa estar dentro de AutenticacaoProvider')
  return valor
}

/** Para telas protegidas por `ExigirSessao`, onde a sessão nunca é nula. */
export function useSessaoAtiva(): Sessao {
  const { sessao } = useAutenticacao()
  if (!sessao) throw new Error('Tela protegida renderizada sem sessão')
  return sessao
}
