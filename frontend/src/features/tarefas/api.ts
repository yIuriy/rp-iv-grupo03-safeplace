import { http } from '../../shared/api/https'
import {
  ehNivelPerigo,
  niveisPerigo,
  rotuloNivelPerigo,
  type NivelPerigo,
} from '../areas-risco/api'

export { niveisPerigo, rotuloNivelPerigo, type NivelPerigo }

export type Tarefa = {
  id: number
  descricao: string
  nivelPerigo: NivelPerigo | null
  classificada: boolean
  dataClassificacao: string | null
}

export type DadosCadastroTarefa = {
  descricao: string
  nivelPerigo?: NivelPerigo
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

  return {
    id: b.id,
    descricao: b.descricao,
    nivelPerigo,
    classificada: b.classificada,
    dataClassificacao,
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
