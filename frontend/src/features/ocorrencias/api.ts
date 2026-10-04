// Contratos e chamadas de ocorrências (UC05/UC09): leitura validada de OcorrenciaResponse, consulta
// por critérios no servidor, detalhe por id, busca de acidente pelo protocolo CAT (issue #128),
// relato, edição, triagem e arquivamento (issue #100).

import { mensagemDaApi, statusDoErro } from '../../shared/api/erros'
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

/**
 * Dados do relato (UC05/UC09) no formato de `CriarOcorrenciaRequest`. A data do fato vai como
 * `AAAA-MM-DDTHH:MM:SS`; a data do registro é gerada pelo servidor. O setor não tem campo próprio:
 * é o da área de risco (decisão DS09, seção 2.3). Campos de acidente e de incidente são exclusivos.
 */
export type DadosOcorrencia = {
  dataOcorrencia: string
  descricao: string
  areaId: number
  colaboradorId: number
  causaRaiz?: string
  gravidade?: string
  tipo?: string
  dano?: string
  destino?: string
  situacaoRisco?: string
  potencialDano?: string
}

export async function registrarOcorrencia(tipoOcorrencia: TipoOcorrencia, dados: DadosOcorrencia): Promise<OcorrenciaResumo> {
  const { data } = await http.post<unknown>('/ocorrencias', { tipoOcorrencia, ...dados })
  return lerOcorrenciaResumo(data)
}

/** Edição pelo Gestor (UC05, alternativo IV): tipo, registro, situação e protocolo são preservados. */
export async function atualizarOcorrencia(id: number, dados: DadosOcorrencia): Promise<OcorrenciaResumo> {
  const { data } = await http.put<unknown>(`/ocorrencias/${id}`, dados)
  return lerOcorrenciaResumo(data)
}

/** Transições aprovadas (decisão DS09, seção 2.6): Aberta → Em triagem → Arquivada. */
export async function enviarParaTriagem(id: number): Promise<OcorrenciaResumo> {
  const { data } = await http.patch<unknown>(`/ocorrencias/${id}/triagem`)
  return lerOcorrenciaResumo(data)
}

export async function arquivarOcorrencia(id: number): Promise<OcorrenciaResumo> {
  const { data } = await http.patch<unknown>(`/ocorrencias/${id}/arquivar`)
  return lerOcorrenciaResumo(data)
}

/** Mensagem para recusas da API, mantendo os dados preenchidos na tela. */
export function descreverFalhaDeOcorrencia(erro: unknown): string {
  const status = statusDoErro(erro)
  if (status === 401) return 'Sua sessão expirou. Entre novamente para continuar.'
  if (status === 403) return 'Seu perfil não tem permissão para esta operação.'
  if (status === 404) return mensagemDaApi(erro) ?? 'Ocorrência não encontrada. Recarregue a lista e tente novamente.'
  if (status === 400 || status === 409 || status === 422) {
    return mensagemDaApi(erro) ?? 'Os dados informados foram recusados pelo servidor. Revise os campos e tente novamente.'
  }
  return 'Não foi possível concluir a operação agora. Os dados preenchidos foram mantidos; verifique sua conexão e tente novamente.'
}

/**
 * Aviso de que os dados de ocorrências mudaram (relato, edição, triagem, arquivamento ou plano), para
 * que a lista e a notificação de novos relatos se atualizem sem recarregar a página.
 */
export const EVENTO_OCORRENCIAS_ALTERADAS = 'safeplace:ocorrencias-alteradas'

export function avisarOcorrenciasAlteradas() {
  window.dispatchEvent(new Event(EVENTO_OCORRENCIAS_ALTERADAS))
}
