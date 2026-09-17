import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { autenticar } from './api'
import { AutenticacaoContexto, type MotivoSaida } from './contexto'
import { aoExpirarSessao, instalarInterceptadores } from './interceptadores'
import { apagarSessao, guardarSessao, sessaoAtual, type Sessao } from './sessao'

// Antes de qualquer tela renderizar: as consultas dos filhos disparam antes dos efeitos do pai.
instalarInterceptadores()

export function AutenticacaoProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(sessaoAtual)
  const [motivoSaida, setMotivoSaida] = useState<MotivoSaida | null>(null)

  const encerrar = useCallback((motivo: MotivoSaida) => {
    apagarSessao()
    setSessao(null)
    setMotivoSaida(motivo)
  }, [])

  useEffect(() => aoExpirarSessao(() => encerrar('expirada')), [encerrar])

  const entrar = useCallback(async (email: string, senha: string) => {
    const nova = await autenticar(email, senha)
    guardarSessao(nova)
    setSessao(nova)
    setMotivoSaida(null)
    return nova
  }, [])

  const valor = useMemo(
    () => ({ sessao, motivoSaida, entrar, sair: () => encerrar('manual') }),
    [sessao, motivoSaida, entrar, encerrar],
  )

  return <AutenticacaoContexto value={valor}>{children}</AutenticacaoContexto>
}
