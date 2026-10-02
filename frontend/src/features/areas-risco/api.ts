// Contratos e chamadas do módulo de áreas de risco (RF05, UC03): áreas, setores físicos e o
// vocabulário NivelPerigo. Os leitores validam a resposta da API antes de entregá-la às telas.

import { http } from '../../shared/api/https'
import type { FeedbackTone } from '../../shared/components'

export type NivelPerigo = 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO'

export const niveisPerigo: readonly NivelPerigo[] = ['BAIXO', 'MEDIO', 'ALTO', 'CRITICO']

export const rotuloNivelPerigo: Record<NivelPerigo, string> = {
  BAIXO: 'Baixo',
  MEDIO: 'Médio',
  ALTO: 'Alto',
  CRITICO: 'Crítico',
}

export const tomDoNivelPerigo: Record<NivelPerigo, FeedbackTone> = {
  BAIXO: 'info',
  MEDIO: 'warning',
  ALTO: 'danger',
  CRITICO: 'danger',
}

export type EpiResumo = {
  id: number
  nome: string
  numeroCa: string
  status: string
}

export type AreaRisco = {
  id: number
  codigo: string
  nome: string
  descricao: string | null
  nivelPerigo: NivelPerigo
  setorId: number | null
  agentesRisco: string[]
  limites: string | null
  episObrigatorios: EpiResumo[]
}

/** Setor físico no formato de `SetorResponse` (diagrama de classes: nome, descrição, localização e status). */
export type Setor = {
  id: number
  nome: string
  descricao: string | null
  localizacao: string | null
  status: NivelPerigo
}

/** Corpo de `POST /api/setores`, igual a `CriarSetorRequest`: só nome e grau são obrigatórios. */
export type DadosCadastroSetor = {
  nome: string
  descricao?: string
  localizacao?: string
  status: NivelPerigo
}

export type DadosCadastroAreaRisco = {
  codigo: string
  nome: string
  descricao?: string
  nivelPerigo: NivelPerigo
  setorId?: number
  agentesRisco: string[]
  limites?: string
  episObrigatoriosIds: number[]
}

export type DadosAtualizacaoAreaRisco = {
  nome: string
  descricao?: string
  nivelPerigo: NivelPerigo
  setorId?: number
  agentesRisco: string[]
  limites?: string
  episObrigatoriosIds: number[]
  justificativa?: string
}

export function ehNivelPerigo(valor: unknown): valor is NivelPerigo {
  return typeof valor === 'string' && niveisPerigo.includes(valor as NivelPerigo)
}

function lerEpiResumo(dado: unknown): EpiResumo {
  if (dado === null || typeof dado !== 'object') throw new Error('EPI resumo inválido na resposta')
  const b = dado as Record<string, unknown>
  if (typeof b.id !== 'number' || typeof b.nome !== 'string' || typeof b.numeroCa !== 'string') {
    throw new Error('EPI resumo incompatível com contrato da API')
  }
  return {
    id: b.id,
    nome: b.nome,
    numeroCa: b.numeroCa,
    status: typeof b.status === 'string' ? b.status : 'DISPONIVEL',
  }
}

function lerAreaRisco(dado: unknown): AreaRisco {
  if (dado === null || typeof dado !== 'object') throw new Error('Área de risco inválida na resposta da API')
  const b = dado as Record<string, unknown>
  if (
    typeof b.id !== 'number' ||
    typeof b.codigo !== 'string' ||
    typeof b.nome !== 'string' ||
    !ehNivelPerigo(b.nivelPerigo)
  ) {
    throw new Error('Área de risco incompatível com contrato da API')
  }
  const agentesRisco = Array.isArray(b.agentesRisco)
    ? b.agentesRisco.filter((a): a is string => typeof a === 'string')
    : []
  const episObrigatorios = Array.isArray(b.episObrigatorios)
    ? b.episObrigatorios.map(lerEpiResumo)
    : []

  return {
    id: b.id,
    codigo: b.codigo,
    nome: b.nome,
    descricao: typeof b.descricao === 'string' ? b.descricao : null,
    nivelPerigo: b.nivelPerigo,
    setorId: typeof b.setorId === 'number' ? b.setorId : null,
    agentesRisco,
    limites: typeof b.limites === 'string' ? b.limites : null,
    episObrigatorios,
  }
}

function lerSetor(dado: unknown): Setor {
  if (dado === null || typeof dado !== 'object') throw new Error('Setor inválido na resposta')
  const b = dado as Record<string, unknown>
  if (typeof b.id !== 'number' || typeof b.nome !== 'string' || !ehNivelPerigo(b.status)) {
    throw new Error('Setor incompatível com contrato da API')
  }
  return {
    id: b.id,
    nome: b.nome,
    descricao: typeof b.descricao === 'string' ? b.descricao : null,
    localizacao: typeof b.localizacao === 'string' ? b.localizacao : null,
    status: b.status,
  }
}

export async function listarAreasRisco(signal?: AbortSignal): Promise<AreaRisco[]> {
  const { data } = await http.get<unknown>('/areas-risco', { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de áreas de risco')
  return data.map(lerAreaRisco)
}

export async function buscarAreaRiscoPorId(id: number, signal?: AbortSignal): Promise<AreaRisco> {
  const { data } = await http.get<unknown>(`/areas-risco/${id}`, { signal })
  return lerAreaRisco(data)
}

export async function cadastrarAreaRisco(dados: DadosCadastroAreaRisco): Promise<AreaRisco> {
  const { data } = await http.post<unknown>('/areas-risco', dados)
  return lerAreaRisco(data)
}

export async function atualizarAreaRisco(id: number, dados: DadosAtualizacaoAreaRisco): Promise<AreaRisco> {
  const { data } = await http.put<unknown>(`/areas-risco/${id}`, dados)
  return lerAreaRisco(data)
}

export async function listarSetores(signal?: AbortSignal): Promise<Setor[]> {
  const { data } = await http.get<unknown>('/setores', { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de setores')
  return data.map(lerSetor)
}

/** Somente o Gestor de Segurança cadastra setores; o `SecurityConfig` recusa os demais perfis. */
export async function cadastrarSetor(dados: DadosCadastroSetor): Promise<Setor> {
  const { data } = await http.post<unknown>('/setores', dados)
  return lerSetor(data)
}

/** Nome e localização do setor, para seletores e cartões; o modelo não atribui código ao `Setor`. */
export function descreverSetor(setor: Setor): string {
  return setor.localizacao ? `${setor.nome} (${setor.localizacao})` : setor.nome
}
