// Contratos e chamadas de ocorrências (UC05/UC09): leitura validada de OcorrenciaResponse, consulta
// por critérios no servidor, detalhe por id e busca de acidente pelo protocolo CAT (issue #128).

import { http } from '../../shared/api/https'

export type TipoOcorrencia = 'ACIDENTE' | 'INCIDENTE'
export type StatusOcorrencia = 'ABERTA' | 'EM_TRIAGEM' | 'ARQUIVADA'

export const rotuloStatusOcorrencia: Record<StatusOcorrencia, string> = {
  ABERTA: 'Aberta',
  EM_TRIAGEM: 'Em triagem',
  ARQUIVADA: 'Arquivada',
}

export type OcorrenciaResumo = {
  idOcorrencia: number
  tipoOcorrencia: TipoOcorrencia
  dataOcorrencia: string
  dataRegistro: string | null
  statusOcorrencia: StatusOcorrencia
  descricao: string
  local?: string | null
  numeroProtocoloCAT?: string | null
  gravidade?: string | null
  causaRaiz?: string | null
  tipo?: string | null
  dano?: string | null
  destino?: string | null
  situacaoRisco?: string | null
  potencialDano?: string | null
  areaId?: number | null
  areaNome?: string | null
  colaboradorId?: number | null
  colaboradorNome?: string | null
  gestorId?: number | null
  gestorNome?: string | null
}

function normalizarStatus(status: unknown): StatusOcorrencia {
  if (typeof status === 'string') {
    const s = status.toUpperCase().replace(/\s+/g, '_')
    if (s === 'EM_TRIAGEM' || s === 'ARQUIVADA') return s
  }
  return 'ABERTA'
}

function lerOcorrenciaResumo(dado: unknown): OcorrenciaResumo {
  if (dado === null || typeof dado !== 'object') throw new Error('Ocorrência inválida na resposta')
  const b = dado as Record<string, unknown>
  const idOcorrencia = typeof b.idOcorrencia === 'number' ? b.idOcorrencia : 0
  const tipoOcorrencia: TipoOcorrencia =
    String(b.tipoOcorrencia).toUpperCase() === 'ACIDENTE' ? 'ACIDENTE' : 'INCIDENTE'

  return {
    idOcorrencia,
    tipoOcorrencia,
    dataOcorrencia: String(b.dataOcorrencia ?? ''),
    dataRegistro: typeof b.dataRegistro === 'string' ? b.dataRegistro : null,
    statusOcorrencia: normalizarStatus(b.statusOcorrencia),
    descricao: String(b.descricao ?? ''),
    local: typeof b.local === 'string' ? b.local : null,
    numeroProtocoloCAT: typeof b.numeroProtocoloCAT === 'string' ? b.numeroProtocoloCAT : null,
    gravidade: typeof b.gravidade === 'string' ? b.gravidade : null,
    causaRaiz: typeof b.causaRaiz === 'string' ? b.causaRaiz : null,
    tipo: typeof b.tipo === 'string' ? b.tipo : null,
    dano: typeof b.dano === 'string' ? b.dano : null,
    destino: typeof b.destino === 'string' ? b.destino : null,
    situacaoRisco: typeof b.situacaoRisco === 'string' ? b.situacaoRisco : null,
    potencialDano: typeof b.potencialDano === 'string' ? b.potencialDano : null,
    areaId: typeof b.areaId === 'number' ? b.areaId : null,
    areaNome: typeof b.areaNome === 'string' ? b.areaNome : null,
    colaboradorId: typeof b.colaboradorId === 'number' ? b.colaboradorId : null,
    colaboradorNome: typeof b.colaboradorNome === 'string' ? b.colaboradorNome : null,
    gestorId: typeof b.gestorId === 'number' ? b.gestorId : null,
    gestorNome: typeof b.gestorNome === 'string' ? b.gestorNome : null,
  }
}

/**
 * Critérios enviados ao servidor em `GET /api/ocorrencias` (issue #128). Datas no formato AAAA-MM-DD,
 * referentes à data do fato; o setor é o da área de risco; o colaborador é o envolvido.
 */
export type FiltroOcorrencias = {
  dataInicio?: string
  dataFim?: string
  setorId?: number
  colaboradorId?: number
}

export function temCriterios(filtro: FiltroOcorrencias): boolean {
  return Object.values(filtro).some(valor => valor !== undefined)
}

export async function listarOcorrencias(filtro: FiltroOcorrencias, signal?: AbortSignal): Promise<OcorrenciaResumo[]> {
  const params = Object.fromEntries(Object.entries(filtro).filter(([, valor]) => valor !== undefined))
  const { data } = await http.get<unknown>('/ocorrencias', { params, signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de ocorrências')
  return data.map(lerOcorrenciaResumo)
}

/** Só acidentes têm protocolo, o número da CAT (decisão DS09, seção 2.1). */
export async function buscarOcorrenciaPorProtocolo(protocolo: string, signal?: AbortSignal): Promise<OcorrenciaResumo> {
  const { data } = await http.get<unknown>(`/ocorrencias/protocolo/${encodeURIComponent(protocolo)}`, { signal })
  return lerOcorrenciaResumo(data)
}

export async function buscarOcorrenciaPorId(id: number, signal?: AbortSignal): Promise<OcorrenciaResumo> {
  const { data } = await http.get<unknown>(`/ocorrencias/${id}`, { signal })
  return lerOcorrenciaResumo(data)
}
