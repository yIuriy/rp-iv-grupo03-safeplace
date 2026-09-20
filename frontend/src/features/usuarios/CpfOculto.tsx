import { useState } from 'react'
import { IconButton } from '../../shared/components'
import { mascararCpf, ocultarCpf } from './formatos'

/** Mostra o CPF com início e fim cobertos por pontos e um botão para revelar o número completo. */
export function CpfOculto({ cpf, nome }: { cpf: string; nome: string }) {
  const [visivel, setVisivel] = useState(false)
  return <span className="sp-cpf-oculto">
    <span>{visivel ? mascararCpf(cpf) : ocultarCpf(cpf)}</span>
    <IconButton variant="quiet" icon={visivel ? 'eye-off' : 'eye'} aria-pressed={visivel}
      label={`${visivel ? 'Ocultar' : 'Exibir'} CPF de ${nome}`} onClick={() => setVisivel(atual => !atual)} />
  </span>
}
