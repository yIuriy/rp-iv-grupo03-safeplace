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
