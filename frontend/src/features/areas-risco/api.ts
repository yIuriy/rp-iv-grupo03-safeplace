// Contratos e chamadas do módulo de áreas de risco (RF05, UC03): áreas, setores físicos, o
// vocabulário NivelPerigo e o histórico de alterações (#114). Os leitores validam a resposta da API
// antes de entregá-la às telas.

import { http } from '../../shared/api/https'
import { lista, listaOuVazia, numeroOuNulo, objeto } from '../../shared/api/leitura'
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
  limites: string
  episObrigatoriosIds: number[]
}

export type DadosAtualizacaoAreaRisco = {
  nome: string
  descricao?: string
  nivelPerigo: NivelPerigo
  setorId?: number
  agentesRisco: string[]
  limites: string
  episObrigatoriosIds: number[]
  justificativa?: string
}

/**
 * Entrada do histórico de alterações no formato de `RegistroHistoricoResponse` (#114), usada por
 * áreas de risco e tarefas. `descricao` traz o que mudou, com valor anterior e novo.
 */
export type RegistroHistorico = {
  id: number
  tipoOperacao: string
  dataHora: string
  responsavel: string
  descricao: string | null
}

export function ehNivelPerigo(valor: unknown): valor is NivelPerigo {
  return typeof valor === 'string' && niveisPerigo.includes(valor as NivelPerigo)
}

export function lerEpiResumo(dado: unknown): EpiResumo {
  const b = objeto(dado, 'EPI resumo inválido na resposta')
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
  const b = objeto(dado, 'Área de risco inválida na resposta da API')
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
  const episObrigatorios = listaOuVazia(b.episObrigatorios, lerEpiResumo)

  return {
    id: b.id,
    codigo: b.codigo,
    nome: b.nome,
    descricao: typeof b.descricao === 'string' ? b.descricao : null,
    nivelPerigo: b.nivelPerigo,
    setorId: numeroOuNulo(b.setorId),
    agentesRisco,
    limites: typeof b.limites === 'string' ? b.limites : null,
    episObrigatorios,
  }
}

function lerRegistroHistorico(dado: unknown): RegistroHistorico {
  const b = objeto(dado, 'Registro de histórico inválido na resposta')
  if (
    typeof b.id !== 'number' ||
    typeof b.tipoOperacao !== 'string' ||
    typeof b.dataHora !== 'string' ||
    typeof b.responsavel !== 'string'
  ) {
    throw new Error('Registro de histórico incompatível com contrato da API')
  }
  return {
    id: b.id,
    tipoOperacao: b.tipoOperacao,
    dataHora: b.dataHora,
    responsavel: b.responsavel,
    descricao: typeof b.descricao === 'string' ? b.descricao : null,
  }
}

/** Valida a lista devolvida pelas rotas `/historico` de áreas de risco e tarefas. */
export function lerHistorico(data: unknown): RegistroHistorico[] {
  return lista(data, lerRegistroHistorico, 'Esperava uma lista de registros de histórico')
}

function lerSetor(dado: unknown): Setor {
  const b = objeto(dado, 'Setor inválido na resposta')
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
  return lista(data, lerAreaRisco, 'Esperava uma lista de áreas de risco')
}

export async function cadastrarAreaRisco(dados: DadosCadastroAreaRisco): Promise<AreaRisco> {
  const { data } = await http.post<unknown>('/areas-risco', dados)
  return lerAreaRisco(data)
}

export async function atualizarAreaRisco(id: number, dados: DadosAtualizacaoAreaRisco): Promise<AreaRisco> {
  const { data } = await http.put<unknown>(`/areas-risco/${id}`, dados)
  return lerAreaRisco(data)
}

/** Somente o Gestor de Segurança consulta o histórico; o `SecurityConfig` recusa os demais perfis. */
export async function listarHistoricoAreaRisco(id: number, signal?: AbortSignal): Promise<RegistroHistorico[]> {
  const { data } = await http.get<unknown>(`/areas-risco/${id}/historico`, { signal })
  return lerHistorico(data)
}

export async function listarSetores(signal?: AbortSignal): Promise<Setor[]> {
  const { data } = await http.get<unknown>('/setores', { signal })
  return lista(data, lerSetor, 'Esperava uma lista de setores')
}

/** Somente o Gestor de Segurança cadastra setores; o `SecurityConfig` recusa os demais perfis. */
export async function cadastrarSetor(dados: DadosCadastroSetor): Promise<Setor> {
  const { data } = await http.post<unknown>('/setores', dados)
  return lerSetor(data)
}

/** Atualização cadastral de setor físico pelo Gestor de Segurança com auditoria (RF05). */
export async function atualizarSetor(id: number, dados: DadosCadastroSetor): Promise<Setor> {
  const { data } = await http.put<unknown>(`/setores/${id}`, dados)
  return lerSetor(data)
}

/** Nome e localização do setor, para seletores e cartões; o modelo não atribui código ao `Setor`. */
export function descreverSetor(setor: Setor): string {
  return setor.localizacao ? `${setor.nome} (${setor.localizacao})` : setor.nome
}
