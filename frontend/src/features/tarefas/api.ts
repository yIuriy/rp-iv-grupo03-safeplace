// Contratos e chamadas do módulo de tarefas (RF06, UC11): matriz de periculosidade, classificação
// com responsável técnico e histórico das classificações (#114).

import { http } from '../../shared/api/https'
import {
  ehNivelPerigo,
  lerEpiResumo,
  lerHistorico,
  niveisPerigo,
  rotuloNivelPerigo,
  type EpiResumo,
  type NivelPerigo,
  type RegistroHistorico,
} from '../areas-risco/api'

export { niveisPerigo, rotuloNivelPerigo, type NivelPerigo, type EpiResumo }

export type Tarefa = {
  id: number
  descricao: string
  nivelPerigo: NivelPerigo | null
  classificada: boolean
  dataClassificacao: string | null
  /** Nome do Gestor responsável técnico pela última classificação; nulo em registros anteriores a #112. */
  responsavelClassificacaoNome: string | null
  areaRiscoId?: number | null
  setoresIds?: number[]
  episIds?: number[]
}

export type DadosCadastroTarefa = {
  descricao: string
  nivelPerigo?: NivelPerigo
  areaRiscoId?: number
  setoresIds?: number[]
}

export type DadosAtualizacaoTarefa = {
  descricao: string
  areaRiscoId?: number
  setoresIds?: number[]
}

export type ItemExigenciaCapacitacao = {
  id: number
  nomeCapacitacao: string
  tipoCapacitacao: 'TREINAMENTO' | 'CERTIFICACAO'
}

export type ExigenciasTarefa = {
  tarefaId: number
  descricao: string
  nivelPerigo: NivelPerigo | null
  classificada: boolean
  capacitacoes: ItemExigenciaCapacitacao[]
  episObrigatorios: EpiResumo[]
}

export type DadosClassificacaoTarefa = {
  nivelPerigo: NivelPerigo
}

function lerTarefa(dado: unknown): Tarefa {
  if (dado === null || typeof dado !== 'object') throw new Error('Tarefa inválida na resposta da API')
  const b = dado as Record<string, unknown>
  if (
    typeof b.id !== 'number' ||
    typeof b.descricao !== 'string' ||
    typeof b.classificada !== 'boolean'
  ) {
    throw new Error('Tarefa incompatível com contrato da API')
  }

  const nivelPerigo = ehNivelPerigo(b.nivelPerigo) ? b.nivelPerigo : null
  const dataClassificacao = typeof b.dataClassificacao === 'string' ? b.dataClassificacao : null
  const responsavelClassificacaoNome =
    typeof b.responsavelClassificacaoNome === 'string' ? b.responsavelClassificacaoNome : null
  const areaRiscoId = typeof b.areaRiscoId === 'number' ? b.areaRiscoId : null
  const setoresIds = Array.isArray(b.setoresIds)
    ? b.setoresIds.filter((item): item is number => typeof item === 'number')
    : []
  const episIds = Array.isArray(b.episIds)
    ? b.episIds.filter((item): item is number => typeof item === 'number')
    : undefined

  return {
    id: b.id,
    descricao: b.descricao,
    nivelPerigo,
    classificada: b.classificada,
    dataClassificacao,
    responsavelClassificacaoNome,
    areaRiscoId,
    setoresIds,
    episIds,
  }
}

export async function listarTarefas(signal?: AbortSignal): Promise<Tarefa[]> {
  const { data } = await http.get<unknown>('/tarefas', { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de tarefas')
  return data.map(lerTarefa)
}

export async function buscarTarefaPorId(id: number, signal?: AbortSignal): Promise<Tarefa> {
  const { data } = await http.get<unknown>(`/tarefas/${id}`, { signal })
  return lerTarefa(data)
}

export async function cadastrarTarefa(dados: DadosCadastroTarefa): Promise<Tarefa> {
  const { data } = await http.post<unknown>('/tarefas', dados)
  return lerTarefa(data)
}

export async function classificarTarefa(
  id: number,
  dados: DadosClassificacaoTarefa
): Promise<Tarefa> {
  const { data } = await http.patch<unknown>(`/tarefas/${id}/classificacao`, dados)
  return lerTarefa(data)
}

/** Somente o Gestor de Segurança consulta o histórico; o `SecurityConfig` recusa os demais perfis. */
export async function listarHistoricoTarefa(id: number, signal?: AbortSignal): Promise<RegistroHistorico[]> {
  const { data } = await http.get<unknown>(`/tarefas/${id}/historico`, { signal })
  return lerHistorico(data)
}

export async function associarEpisTarefa(id: number, epiIds: number[]): Promise<Tarefa> {
  const { data } = await http.put<unknown>(`/tarefas/${id}/epis`, { epiIds })
  return lerTarefa(data)
}

export async function listarEpisTarefa(id: number, signal?: AbortSignal): Promise<EpiResumo[]> {
  const { data } = await http.get<unknown>(`/tarefas/${id}/epis`, { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de EPIs')
  return data.map(lerEpiResumo)
}

export async function atualizarTarefa(id: number, dados: DadosAtualizacaoTarefa): Promise<Tarefa> {
  const { data } = await http.put<unknown>(`/tarefas/${id}`, dados)
  return lerTarefa(data)
}

function lerItemExigenciaCapacitacao(dado: unknown): ItemExigenciaCapacitacao {
  if (dado === null || typeof dado !== 'object') throw new Error('Exigência de capacitação inválida')
  const b = dado as Record<string, unknown>
  return {
    id: typeof b.id === 'number' ? b.id : 0,
    nomeCapacitacao: typeof b.nomeCapacitacao === 'string' ? b.nomeCapacitacao : '',
    tipoCapacitacao: b.tipoCapacitacao === 'CERTIFICACAO' ? 'CERTIFICACAO' : 'TREINAMENTO',
  }
}

function lerExigenciasTarefa(dado: unknown): ExigenciasTarefa {
  if (dado === null || typeof dado !== 'object') throw new Error('Resposta de exigências inválida')
  const b = dado as Record<string, unknown>
  return {
    tarefaId: typeof b.tarefaId === 'number' ? b.tarefaId : 0,
    descricao: typeof b.descricao === 'string' ? b.descricao : '',
    nivelPerigo: ehNivelPerigo(b.nivelPerigo) ? b.nivelPerigo : null,
    classificada: typeof b.classificada === 'boolean' ? b.classificada : false,
    capacitacoes: Array.isArray(b.capacitacoes) ? b.capacitacoes.map(lerItemExigenciaCapacitacao) : [],
    episObrigatorios: Array.isArray(b.episObrigatorios) ? b.episObrigatorios.map(lerEpiResumo) : [],
  }
}

export async function obterExigenciasTarefa(id: number, signal?: AbortSignal): Promise<ExigenciasTarefa> {
  const { data } = await http.get<unknown>(`/tarefas/${id}/exigencias`, { signal })
  return lerExigenciasTarefa(data)
}
