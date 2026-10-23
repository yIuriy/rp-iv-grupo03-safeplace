// Utilitários para ler erros do axios (status HTTP e a mensagem devolvida pela API no campo
// message) e para transformar uma falha em mensagem para o usuário com o padrão Strategy.

import { isAxiosError } from 'axios'

export function statusDoErro(erro: unknown): number | undefined {
  return isAxiosError(erro) ? erro.response?.status : undefined
}

/** Mensagem devolvida pela API no campo `message`, padrão dos tratadores de exceção do backend. */
export function mensagemDaApi(erro: unknown): string | undefined {
  if (!isAxiosError(erro)) return undefined
  const corpo: unknown = erro.response?.data
  if (corpo === null || typeof corpo !== 'object') return undefined
  const mensagem = (corpo as { message?: unknown }).message
  return typeof mensagem === 'string' && mensagem.trim() ? mensagem : undefined
}

/**
 * Strategy (GoF): regra que transforma uma falha HTTP na mensagem exibida ao usuário. Um texto
 * fixo é a estratégia mais simples; uma função permite consultar o próprio erro.
 */
export type EstrategiaDeMensagem = string | ((erro: unknown) => string)

/** ConcreteStrategy: usa a mensagem enviada pela API e, sem ela, o texto alternativo. */
export function usarMensagemDaApi(alternativa: string): EstrategiaDeMensagem {
  return (erro) => mensagemDaApi(erro) ?? alternativa
}

/**
 * Context: escolhe a estratégia pelo status HTTP da falha e a executa. Status sem estratégia
 * própria, ou erro que não veio do servidor, usa a estratégia padrão. Cada tela informa apenas
 * as suas estratégias; o algoritmo de escolha existe só aqui.
 */
export function criarDescritorDeFalha(
  estrategiasPorStatus: Readonly<Partial<Record<number, EstrategiaDeMensagem>>>,
  padrao: EstrategiaDeMensagem,
): (erro: unknown) => string {
  return (erro) => {
    const status = statusDoErro(erro)
    const escolhida = (status !== undefined && estrategiasPorStatus[status]) || padrao
    return typeof escolhida === 'string' ? escolhida : escolhida(erro)
  }
}
