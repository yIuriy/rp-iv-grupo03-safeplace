import { useCallback, useEffect, useState } from 'react'
import { defaultCacheStorage, type CacheStoragePort } from './cacheStorage'
import { sessaoAtual } from '../../features/autenticacao/sessao'

export type ConsultaOffline<T> = {
  dados: T | null
  erro: boolean
  carregando: boolean
  /** `true` enquanto `dados` for uma cópia local ainda não confirmada pelo servidor. */
  isOffline: boolean
  sincronizadoEm: Date | null
  recarregar: () => void
}

type Resultado<T> = {
  chave: string
  tentativa: number
  dados?: T
  erro?: boolean
  isOffline?: boolean
  sincronizadoEm?: Date | null
}

/**
 * Consulta com cópia local por conta (RNF08), restrita a leitura: sem sessão não há cópia, e a cópia
 * só deixa de ser sinalizada como local quando o servidor confirma. Ao voltar a rede, recarrega.
 * Desacoplada do armazenamento via CacheStoragePort (DIP).
 */
export function useConsultaOffline<T>(
  chave: string,
  carregar: (signal: AbortSignal) => Promise<T>,
  storage: CacheStoragePort = defaultCacheStorage
): ConsultaOffline<T> {
  const [resultado, setResultado] = useState<Resultado<T> | null>(null)
  const [tentativa, setTentativa] = useState(0)
  const recarregar = useCallback(() => setTentativa(anterior => anterior + 1), [])

  useEffect(() => {
    window.addEventListener('online', recarregar)
    return () => window.removeEventListener('online', recarregar)
  }, [recarregar])

  useEffect(() => {
    const controle = new AbortController()
    const emailConta = sessaoAtual()?.email

    carregar(controle.signal)
      .then(dados => {
        if (controle.signal.aborted) return
        if (emailConta) storage.setItem(chave, emailConta, dados)
        setResultado({ chave, tentativa, dados, isOffline: false, sincronizadoEm: new Date() })
      })
      .catch(() => {
        if (controle.signal.aborted) return
        const cache = emailConta ? storage.getItem<T>(chave, emailConta) : null
        setResultado(cache
          ? { chave, tentativa, dados: cache.dados, isOffline: true, sincronizadoEm: new Date(cache.timestamp) }
          : { chave, tentativa, erro: true, isOffline: false })
      })

    return () => controle.abort()
  }, [chave, tentativa, carregar, storage])

  // Durante uma nova tentativa a última cópia segue visível, sem ser promovida a dado atual.
  const visivel = resultado?.chave === chave ? resultado : null
  const concluida = visivel?.tentativa === tentativa

  return {
    dados: visivel?.dados ?? null,
    erro: concluida && visivel?.erro === true,
    carregando: !concluida,
    isOffline: visivel?.isOffline === true,
    sincronizadoEm: visivel?.sincronizadoEm ?? null,
    recarregar,
  }
}
