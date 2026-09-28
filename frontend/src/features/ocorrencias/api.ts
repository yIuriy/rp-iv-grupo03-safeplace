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

export async function listarOcorrencias(signal?: AbortSignal): Promise<OcorrenciaResumo[]> {
  const { data } = await http.get<unknown>('/ocorrencias', { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de ocorrências')
  return data.map(lerOcorrenciaResumo)
}

export async function buscarOcorrenciaPorId(id: number, signal?: AbortSignal): Promise<OcorrenciaResumo> {
  const { data } = await http.get<unknown>(`/ocorrencias/${id}`, { signal })
  return lerOcorrenciaResumo(data)
}
