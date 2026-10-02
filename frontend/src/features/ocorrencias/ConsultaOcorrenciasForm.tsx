// Formulário de consulta de ocorrências (UC05/UC09, issue #128). Período da data do fato, setor e
// colaborador envolvido vão ao servidor; texto, tipo e situação refinam a lista já recebida. Período
// invertido é barrado antes do envio, com foco no campo a corrigir.

import { useState, type FormEvent } from 'react'
import { Button, SearchInput, Select, TextField } from '../../shared/components'
import type { Consulta } from '../../shared/api/useConsulta'
import { descreverSetor, type Setor } from '../areas-risco/api'
import type { Usuario } from '../usuarios/api'
import { hojeIso } from '../usuarios/formatos'
import { rotuloStatusOcorrencia, type StatusOcorrencia } from './api'
import { lerCriterios, type CriteriosConsulta } from './criteriosConsulta'
import { rotuloTipoOcorrencia } from './formatos'

function ajudaDaLista<T>(consulta: Consulta<T[]>, rotulo: string): string | undefined {
  if (consulta.erro) return `Não foi possível carregar ${rotulo}. Os demais critérios continuam disponíveis.`
  return consulta.carregando ? `Carregando ${rotulo}...` : undefined
}

export function ConsultaOcorrenciasForm({ setores, colaboradores, aoConsultar, aoLimpar }: {
  setores: Consulta<Setor[]>
  colaboradores: Consulta<Usuario[]>
  aoConsultar: (criterios: CriteriosConsulta) => void
  aoLimpar: () => void
}) {
  const [erroPeriodo, setErroPeriodo] = useState<string>()

  function consultar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const leitura = lerCriterios(new FormData(form))
    setErroPeriodo(leitura.erroPeriodo)
    if (leitura.erroPeriodo) {
      const campo = form.elements.namedItem('dataFim')
      if (campo instanceof HTMLElement) campo.focus()
      return
    }
    aoConsultar(leitura.criterios)
  }

  return <form className="sp-consulta-ocorrencias" role="search" aria-label="Consultar ocorrências" noValidate
    onSubmit={consultar} onReset={() => { setErroPeriodo(undefined); aoLimpar() }}>
    <SearchInput label="Buscar na lista" name="busca" className="sp-consulta-texto" hiddenLabel={false}
      placeholder="Descrição, protocolo, colaborador ou área" autoComplete="off" />
    <Select label="Tipo" name="tipoOcorrencia" defaultValue="">
      <option value="">Todos os tipos</option>
      {(['ACIDENTE', 'INCIDENTE'] as const).map(tipo => <option key={tipo} value={tipo}>{rotuloTipoOcorrencia[tipo]}</option>)}
    </Select>
    <Select label="Situação" name="statusOcorrencia" defaultValue="">
      <option value="">Todas as situações</option>
      {(Object.keys(rotuloStatusOcorrencia) as StatusOcorrencia[])
        .map(status => <option key={status} value={status}>{rotuloStatusOcorrencia[status]}</option>)}
    </Select>
    <TextField label="Data do fato: de" name="dataInicio" type="date" max={hojeIso()} />
    <TextField label="Data do fato: até" name="dataFim" type="date" max={hojeIso()} error={erroPeriodo} />
    <Select label="Setor" name="setorId" defaultValue="" disabled={!setores.dados}
      helper={ajudaDaLista(setores, 'os setores')}>
      <option value="">Todos os setores</option>
      {setores.dados?.map(setor => <option key={setor.id} value={setor.id}>{descreverSetor(setor)}</option>)}
    </Select>
    <Select label="Colaborador envolvido" name="colaboradorId" defaultValue="" disabled={!colaboradores.dados}
      helper={ajudaDaLista(colaboradores, 'os colaboradores')}>
      <option value="">Todos os colaboradores</option>
      {colaboradores.dados?.map(pessoa => <option key={pessoa.id} value={pessoa.id}>{pessoa.nome}</option>)}
    </Select>
    <div className="sp-acoes-busca">
      <Button type="submit" variant="secondary" icon="search">Consultar</Button>
      <Button type="reset" variant="quiet">Limpar</Button>
    </div>
  </form>
}
