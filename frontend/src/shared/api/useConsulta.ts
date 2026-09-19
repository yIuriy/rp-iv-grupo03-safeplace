import { useCallback, useEffect, useState } from 'react'

export type Consulta<T> = {
  dados: T | null
  erro: boolean
  carregando: boolean
  recarregar: () => void
}

type Resultado<T> = { chave: string; tentativa: number; dados?: T; erro?: true }

/**
 * Executa `carregar` quando `chave`, `tentativa` ou a própria função mudam e cancela a
 * requisição anterior. `carregar` precisa ser estável (função de módulo ou `useCallback`),
 * senão cada renderização dispara uma nova consulta.
 */
export function useConsulta<T>(chave: string, carregar: (signal: AbortSignal) => Promise<T>): Consulta<T> {
  const [resultado, setResultado] = useState<Resultado<T> | null>(null)
  const [tentativa, setTentativa] = useState(0)

  useEffect(() => {
    const controle = new AbortController()
    carregar(controle.signal)
      .then(dados => { if (!controle.signal.aborted) setResultado({ chave, tentativa, dados }) })
      .catch(() => { if (!controle.signal.aborted) setResultado({ chave, tentativa, erro: true }) })
    return () => controle.abort()
  }, [chave, tentativa, carregar])

  const atual = resultado?.chave === chave && resultado.tentativa === tentativa ? resultado : null
  const recarregar = useCallback(() => setTentativa(anterior => anterior + 1), [])
  return { dados: atual?.dados ?? null, erro: atual?.erro === true, carregando: atual === null, recarregar }
}
