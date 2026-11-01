// Hook do Template Method de envio (#233): guarda erros, falha e "enviando" no estado do React e
// entrega um onSubmit que roda o esqueleto de executarEnvio com as etapas de cada formulário.
// Substitui o useState de enviando e o try/catch que cada formulário repetia.

import { useRef, useState, type FormEvent } from 'react'
import { camposInvalidos, executarEnvio, focarCampo, type EtapasDoEnvio } from './envioFormulario'

export function useEnvioFormulario<L, D, E>(etapas: EtapasDoEnvio<L, D, E>, errosIniciais: E) {
  const [erros, setErros] = useState<E>(errosIniciais)
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)
  // A referência bloqueia o segundo envio mesmo antes de o React redesenhar o botão.
  const emAndamento = useRef(false)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (emAndamento.current) return
    emAndamento.current = true
    const form = event.currentTarget
    const resultado = await executarEnvio(form, etapas, {
      definirErros: setErros,
      definirFalha: setFalha,
      definirEnviando: setEnviando,
      focar: campo => focarCampo(form, campo),
    })
    if (resultado !== 'enviado' || etapas.liberarAposSucesso) emAndamento.current = false
  }

  const temPendencias = camposInvalidos(etapas.ordem, erros).length > 0
  return { enviando, erros, falha, temPendencias, onSubmit }
}
