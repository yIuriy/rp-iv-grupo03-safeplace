// Consulta de acidente pelo protocolo CAT (issue #128). Só acidentes têm protocolo (decisão DS09,
// seção 2.1): protocolo inexistente ou fora do perfil gera aviso claro, e o encontrado abre o detalhe.

import { useState, type FormEvent } from 'react'
import { Alert, Button, TextField } from '../../shared/components'
import { statusDoErro } from '../../shared/api/erros'
import { buscarOcorrenciaPorProtocolo } from './api'

export function ProtocoloBusca({ aoEncontrar, desabilitado }: { aoEncontrar: (idOcorrencia: number) => void; desabilitado: boolean }) {
  const [erroCampo, setErroCampo] = useState<string>()
  const [aviso, setAviso] = useState<{ titulo: string; texto: string }>()
  const [buscando, setBuscando] = useState(false)

  async function buscar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const campo = event.currentTarget.elements.namedItem('protocolo')
    const protocolo = String(new FormData(event.currentTarget).get('protocolo') ?? '').trim()
    setAviso(undefined)
    if (!protocolo) {
      setErroCampo('Informe o protocolo CAT.')
      if (campo instanceof HTMLElement) campo.focus()
      return
    }
    setErroCampo(undefined)
    setBuscando(true)
    try {
      const ocorrencia = await buscarOcorrenciaPorProtocolo(protocolo)
      aoEncontrar(ocorrencia.idOcorrencia)
    } catch (erro) {
      setAviso(statusDoErro(erro) === 404
        ? { titulo: 'Protocolo não encontrado', texto: `Nenhum acidente com o protocolo ${protocolo} está entre os relatos que seu perfil pode consultar. Incidentes não têm protocolo: use os critérios da consulta.` }
        : { titulo: 'Não foi possível consultar o protocolo', texto: 'Verifique sua conexão e tente novamente.' })
    } finally {
      setBuscando(false)
    }
  }

  return <div className="sp-protocolo">
    <form className="sp-protocolo-busca" role="search" aria-label="Consultar acidente pelo protocolo CAT" noValidate onSubmit={buscar}>
      <TextField label="Protocolo CAT" name="protocolo" autoComplete="off" placeholder="CAT-2026-09-0001"
        error={erroCampo} helper={erroCampo ? undefined : 'Só acidentes têm protocolo, o número da CAT.'} />
      <Button type="submit" variant="secondary" loading={buscando} disabled={desabilitado}
        title={desabilitado ? 'Consulta por protocolo indisponível em modo offline' : undefined}>Consultar protocolo</Button>
    </form>
    {aviso && <Alert tone="warning" title={aviso.titulo} live="polite">{aviso.texto}</Alert>}
  </div>
}
