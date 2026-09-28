import { useCallback, useEffect, useState } from 'react'
import { defaultCacheStorage, type CacheStoragePort } from './cacheStorage'
import { sessaoAtual } from '../../features/autenticacao/sessao'

export type ConsultaOffline<T> = {
  dados: T | null
  erro: boolean
  carregando: boolean
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
 * Hook de consulta com fallback para cache local resiliente por conta (RNF08).
 * SOLID: Desacoplado da camada de armazenamento via CacheStoragePort (DIP).
 */
export function useConsultaOffline<T>(
  chave: string,
  carregar: (signal: AbortSignal) => Promise<T>,
  storage: CacheStoragePort = defaultCacheStorage
): ConsultaOffline<T> {
  const [resultado, setResultado] = useState<Resultado<T> | null>(null)
  const [tentativa, setTentativa] = useState(0)

  useEffect(() => {
    const controle = new AbortController()
    const emailConta = sessaoAtual()?.email ?? 'anonimo'

    carregar(controle.signal)
      .then(dados => {
        if (!controle.signal.aborted) {
          // Persiste cópia atualizada no cache local isolado por conta
          storage.setItem(chave, emailConta, dados)
          setResultado({
            chave,
            tentativa,
            dados,
            isOffline: false,
            sincronizadoEm: new Date(),
          })
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          // Em caso de falha de rede/timeout, busca fallback no cache da conta
          const cache = storage.getItem<T>(chave, emailConta)
          if (cache) {
            setResultado({
              chave,
              tentativa,
              dados: cache.dados,
              isOffline: true,
              sincronizadoEm: new Date(cache.timestamp),
            })
          } else {
            setResultado({
              chave,
              tentativa,
              erro: true,
              isOffline: typeof navigator !== 'undefined' ? !navigator.onLine : false,
            })
          }
        }
      })

    return () => controle.abort()
  }, [chave, tentativa, carregar, storage])

  const atual = resultado?.chave === chave && resultado.tentativa === tentativa ? resultado : null
  const recarregar = useCallback(() => setTentativa(anterior => anterior + 1), [])

  return {
    dados: atual?.dados ?? null,
    erro: atual?.erro === true,
    carregando: atual === null,
    isOffline: atual?.isOffline === true,
    sincronizadoEm: atual?.sincronizadoEm ?? null,
    recarregar,
  }
}
