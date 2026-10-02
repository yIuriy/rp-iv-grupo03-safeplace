// Leitura e validação dos critérios da consulta de ocorrências (issue #128): separa os critérios
// enviados ao servidor dos que refinam a lista na tela e barra período invertido antes do envio.

import type { FiltroOcorrencias, StatusOcorrencia, TipoOcorrencia } from './api'

export type CriteriosLocais = { texto: string; tipo: '' | TipoOcorrencia; status: '' | StatusOcorrencia }
export type CriteriosConsulta = { locais: CriteriosLocais; filtro: FiltroOcorrencias }

export const SEM_CRITERIOS: CriteriosConsulta = { locais: { texto: '', tipo: '', status: '' }, filtro: {} }

function numeroOuNada(valor: FormDataEntryValue | null): number | undefined {
  const texto = String(valor ?? '')
  return texto ? Number(texto) : undefined
}

function textoOuNada(valor: FormDataEntryValue | null): string | undefined {
  const texto = String(valor ?? '').trim()
  return texto || undefined
}

/** Lê o formulário; devolve o erro do período quando a data inicial passa da final. */
export function lerCriterios(form: FormData): { criterios: CriteriosConsulta; erroPeriodo?: string } {
  const filtro: FiltroOcorrencias = {
    dataInicio: textoOuNada(form.get('dataInicio')),
    dataFim: textoOuNada(form.get('dataFim')),
    setorId: numeroOuNada(form.get('setorId')),
    colaboradorId: numeroOuNada(form.get('colaboradorId')),
  }
  const locais: CriteriosLocais = {
    texto: String(form.get('busca') ?? '').trim(),
    tipo: String(form.get('tipoOcorrencia') ?? '') as CriteriosLocais['tipo'],
    status: String(form.get('statusOcorrencia') ?? '') as CriteriosLocais['status'],
  }
  const invertido = filtro.dataInicio && filtro.dataFim && filtro.dataInicio > filtro.dataFim
  return { criterios: { locais, filtro }, erroPeriodo: invertido ? 'A data final deve ser igual ou posterior à data inicial.' : undefined }
}
