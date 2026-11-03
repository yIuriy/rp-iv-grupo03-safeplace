// Contratos e chamadas do plano de ação de uma ocorrência (UC07, contrato da issue #97): cada
// PlanoDeAcao é uma ação com medida corretiva e/ou preventiva, prazo, um responsável (colaborador ou
// setor), situação ABERTA → CONCLUIDA e alerta com antecedência configurável. Atraso e alerta devido
// não são estados gravados: o servidor os calcula a partir do prazo e da data de hoje (issue #196).

import { http } from '../../shared/api/https'
import { lista, numeroOuNulo, objeto, textoOuNulo } from '../../shared/api/leitura'

export type SituacaoAcao = 'ABERTA' | 'CONCLUIDA'
export type TipoResponsavel = 'COLABORADOR' | 'SETOR'

export const rotuloSituacaoAcao: Record<SituacaoAcao, string> = {
  ABERTA: 'Aberta',
  CONCLUIDA: 'Concluída',
}

export type AcaoPlano = {
  id: number
  idOcorrencia: number | null
  medidasCorretivas: string | null
  medidasPreventivas: string | null
  prazo: string
  /** `null` quando o texto gravado é anterior ao contrato e não foi reconhecido (decisão 2.8). */
  situacao: SituacaoAcao | null
  statusGravado: string
  responsavelTipo: TipoResponsavel | null
  responsavelId: number | null
  responsavelNome: string | null
  alertaAtivo: boolean
  antecedenciaAlertaDias: number | null
  dataAlerta: string | null
  /** Calculado pelo servidor: ação aberta que já chegou à antecedência configurada, inclusive após o prazo. */
  alertaDevido: boolean
  /** Calculado pelo servidor: ação aberta com prazo anterior a hoje. */
  emAtraso: boolean
  gestorNome: string | null
}

export type Prorrogacao = {
  idAcao: number
  prazoAnterior: string
  novoPrazo: string
  justificativa: string
}

function lerAcao(dado: unknown): AcaoPlano {
  const b = objeto(dado, 'Ação do plano inválida na resposta')
  if (typeof b.id !== 'number' || typeof b.prazo !== 'string') throw new Error('Ação do plano incompatível com o contrato da API')
  const situacao = String(b.situacao ?? '').toUpperCase()
  const tipo = String(b.responsavelTipo ?? '').toUpperCase()
  return {
    id: b.id,
    idOcorrencia: numeroOuNulo(b.idOcorrencia),
    medidasCorretivas: textoOuNulo(b.medidasCorretivas),
    medidasPreventivas: textoOuNulo(b.medidasPreventivas),
    prazo: b.prazo,
    situacao: situacao === 'ABERTA' || situacao === 'CONCLUIDA' ? situacao : null,
    statusGravado: String(b.status ?? b.situacao ?? ''),
    responsavelTipo: tipo === 'COLABORADOR' || tipo === 'SETOR' ? tipo : null,
    responsavelId: numeroOuNulo(b.responsavelId),
    responsavelNome: textoOuNulo(b.responsavelNome),
    alertaAtivo: b.alertaAtivo === true,
    antecedenciaAlertaDias: numeroOuNulo(b.antecedenciaAlertaDias),
    dataAlerta: textoOuNulo(b.dataAlerta),
    alertaDevido: b.alertaDevido === true,
    emAtraso: b.emAtraso === true,
    gestorNome: textoOuNulo(b.gestorNome),
  }
}

function lerProrrogacao(dado: unknown): Prorrogacao {
  const b = objeto(dado, 'Prorrogação inválida na resposta')
  return {
    idAcao: typeof b.idAcao === 'number' ? b.idAcao : 0,
    prazoAnterior: String(b.prazoAnterior ?? ''),
    novoPrazo: String(b.novoPrazo ?? ''),
    justificativa: String(b.justificativa ?? ''),
  }
}

export async function listarAcoes(idOcorrencia: number, signal?: AbortSignal): Promise<AcaoPlano[]> {
  const { data } = await http.get<unknown>(`/ocorrencias/${idOcorrencia}/planos-acao`, { signal })
  return lista(data, lerAcao, 'Esperava a lista de ações do plano')
}

/** Corpo de `CriarPlanoDeAcaoRequest`: exatamente um entre colaborador e setor responsável. */
export type DadosNovaAcao = {
  medidasCorretivas?: string
  medidasPreventivas?: string
  prazo: string
  responsavelColaboradorId?: number
  responsavelSetorId?: number
  alertaAtivo: boolean
  antecedenciaAlertaDias?: number
}

export async function cadastrarAcao(idOcorrencia: number, dados: DadosNovaAcao): Promise<AcaoPlano> {
  const { data } = await http.post<unknown>(`/ocorrencias/${idOcorrencia}/planos-acao`, dados)
  return lerAcao(data)
}

export async function concluirAcao(id: number): Promise<AcaoPlano> {
  const { data } = await http.patch<unknown>(`/planos-acao/${id}/status`, { status: 'CONCLUIDA' })
  return lerAcao(data)
}

export async function prorrogarPrazo(id: number, novoPrazo: string, justificativa: string): Promise<Prorrogacao> {
  const { data } = await http.post<unknown>(`/planos-acao/${id}/prorrogacoes`, { novoPrazo, justificativa })
  return lerProrrogacao(data)
}

export async function listarProrrogacoes(id: number, signal?: AbortSignal): Promise<Prorrogacao[]> {
  const { data } = await http.get<unknown>(`/planos-acao/${id}/prorrogacoes`, { signal })
  return lista(data, lerProrrogacao, 'Esperava o histórico de prorrogações')
}

export async function reatribuirResponsavel(id: number, responsavel: { colaboradorId?: number; setorId?: number }): Promise<AcaoPlano> {
  const { data } = await http.patch<unknown>(`/planos-acao/${id}/responsavel`, responsavel)
  return lerAcao(data)
}

export async function configurarAlerta(id: number, ativo: boolean, antecedenciaDias?: number): Promise<AcaoPlano> {
  const { data } = await http.patch<unknown>(`/planos-acao/${id}/alerta`, { ativo, antecedenciaDias })
  return lerAcao(data)
}
