// Cliente tipado da API de capacitações (/api/capacitacoes, issue #119): listagem com filtro por
// situação, detalhes do colaborador, alertas de vencimento e consulta de aptidão para uma tarefa
// (RF10, UC02, US10). Só leitura: o cadastro aguarda o fluxo de #116 e #126.

import { http } from '../../shared/api/https'
import { mensagemDaApi, statusDoErro } from '../../shared/api/erros'

/** Situação de `Certificacao.status` calculada pelo backend (UC02, cenário alternativo IV). */
export type SituacaoCertificacao = 'EM_DIA' | 'PROXIMA_DO_VENCIMENTO' | 'VENCIDA'

export const situacoesCertificacao: readonly SituacaoCertificacao[] = ['EM_DIA', 'PROXIMA_DO_VENCIMENTO', 'VENCIDA']

export type ResumoColaborador = {
  colaboradorId: number
  colaboradorNome: string
  /** Mais crítica entre as certificações; nula quando não há certificação cadastrada. */
  situacaoCertificacoes: SituacaoCertificacao | null
  semCertificacoes: boolean
  totalCertificacoes: number
  totalTreinamentos: number
  pendenteIntegracaoAdmissional: boolean
}

/** Atributos de `Certificacao` no diagrama de classes. */
export type Certificacao = {
  id: number
  nome: string
  status: SituacaoCertificacao
  dataVencimento: string
  dataEmissao: string
}

/** Atributos de `Treinamento` no diagrama de classes. Treinamento não tem data de validade. */
export type Treinamento = {
  id: number
  nome: string
  conteudo: string | null
  instrutor: string | null
  dataRealizacao: string
  cargaHoraria: number | null
  status: 'REALIZADO' | 'PENDENTE'
}

export type CapacitacoesColaborador = {
  colaboradorId: number
  colaboradorNome: string
  situacaoCertificacoes: SituacaoCertificacao | null
  certificacoes: Certificacao[]
  treinamentos: Treinamento[]
  certificacoesVencidas: Certificacao[]
  certificacoesProximasDoVencimento: Certificacao[]
  pendenteIntegracaoAdmissional: boolean
  pendencias: string[]
}

export type AlertaCertificacao = {
  certificacaoId: number
  nome: string
  status: SituacaoCertificacao
  dataVencimento: string
  dataEmissao: string
  colaboradorId: number
  colaboradorNome: string
}

export type PendenciaColaborador = { colaboradorId: number; colaboradorNome: string; mensagens: string[] }

export type AlertasCapacitacao = {
  dataReferencia: string
  diasAntecedenciaAlerta: number
  proximasDoVencimento: AlertaCertificacao[]
  vencidas: AlertaCertificacao[]
  pendencias: PendenciaColaborador[]
}

/** Resultado informativo da regra de #117; não autoriza a alocação. */
export type Aptidao = {
  apto: boolean
  tarefaId: number
  tarefaDescricao: string
  colaboradorId: number
  colaboradorNome: string
  impedimentos: string[]
}

type Bruto = Record<string, unknown>

function objeto(dado: unknown, contexto: string): Bruto {
  if (dado === null || typeof dado !== 'object' || Array.isArray(dado)) {
    throw new Error(`${contexto} inválido na resposta da API`)
  }
  return dado as Bruto
}

function lista<T>(dado: unknown, ler: (item: unknown) => T, contexto: string): T[] {
  if (!Array.isArray(dado)) throw new Error(`Esperava uma lista de ${contexto}`)
  return dado.map(ler)
}

function texto(valor: unknown): string | null {
  return typeof valor === 'string' && valor.trim() ? valor : null
}

function textos(valor: unknown): string[] {
  return Array.isArray(valor) ? valor.filter((item): item is string => typeof item === 'string') : []
}

function ehSituacao(valor: unknown): valor is SituacaoCertificacao {
  return situacoesCertificacao.includes(valor as SituacaoCertificacao)
}

function situacaoOuNula(valor: unknown): SituacaoCertificacao | null {
  return ehSituacao(valor) ? valor : null
}

function exigir(condicao: boolean, contexto: string): void {
  if (!condicao) throw new Error(`${contexto} incompatível com o contrato da API`)
}

function lerResumo(dado: unknown): ResumoColaborador {
  const b = objeto(dado, 'Colaborador')
  exigir(typeof b.colaboradorId === 'number' && typeof b.colaboradorNome === 'string', 'Colaborador')
  return {
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: b.colaboradorNome as string,
    situacaoCertificacoes: situacaoOuNula(b.situacaoCertificacoes),
    semCertificacoes: b.semCertificacoes === true,
    totalCertificacoes: typeof b.totalCertificacoes === 'number' ? b.totalCertificacoes : 0,
    totalTreinamentos: typeof b.totalTreinamentos === 'number' ? b.totalTreinamentos : 0,
    pendenteIntegracaoAdmissional: b.pendenteIntegracaoAdmissional === true,
  }
}

function lerCertificacao(dado: unknown): Certificacao {
  const b = objeto(dado, 'Certificação')
  exigir(typeof b.id === 'number' && typeof b.nome === 'string' && ehSituacao(b.status)
    && typeof b.dataVencimento === 'string' && typeof b.dataEmissao === 'string', 'Certificação')
  return {
    id: b.id as number,
    nome: b.nome as string,
    status: b.status as SituacaoCertificacao,
    dataVencimento: b.dataVencimento as string,
    dataEmissao: b.dataEmissao as string,
  }
}

function lerTreinamento(dado: unknown): Treinamento {
  const b = objeto(dado, 'Treinamento')
  exigir(typeof b.id === 'number' && typeof b.nome === 'string' && typeof b.dataRealizacao === 'string', 'Treinamento')
  return {
    id: b.id as number,
    nome: b.nome as string,
    conteudo: texto(b.conteudo),
    instrutor: texto(b.instrutor),
    dataRealizacao: b.dataRealizacao as string,
    cargaHoraria: typeof b.cargaHoraria === 'number' ? b.cargaHoraria : null,
    status: b.status === 'PENDENTE' ? 'PENDENTE' : 'REALIZADO',
  }
}

function lerDetalhe(dado: unknown): CapacitacoesColaborador {
  const b = objeto(dado, 'Capacitações do colaborador')
  exigir(typeof b.colaboradorId === 'number' && typeof b.colaboradorNome === 'string', 'Capacitações do colaborador')
  return {
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: b.colaboradorNome as string,
    situacaoCertificacoes: situacaoOuNula(b.situacaoCertificacoes),
    certificacoes: lista(b.certificacoes ?? [], lerCertificacao, 'certificações'),
    treinamentos: lista(b.treinamentos ?? [], lerTreinamento, 'treinamentos'),
    certificacoesVencidas: lista(b.certificacoesVencidas ?? [], lerCertificacao, 'certificações vencidas'),
    certificacoesProximasDoVencimento: lista(b.certificacoesProximasDoVencimento ?? [], lerCertificacao,
      'certificações próximas do vencimento'),
    pendenteIntegracaoAdmissional: b.pendenteIntegracaoAdmissional === true,
    pendencias: textos(b.pendencias),
  }
}

function lerAlerta(dado: unknown): AlertaCertificacao {
  const b = objeto(dado, 'Alerta de certificação')
  exigir(typeof b.certificacaoId === 'number' && typeof b.nome === 'string' && ehSituacao(b.status)
    && typeof b.dataVencimento === 'string' && typeof b.colaboradorId === 'number', 'Alerta de certificação')
  return {
    certificacaoId: b.certificacaoId as number,
    nome: b.nome as string,
    status: b.status as SituacaoCertificacao,
    dataVencimento: b.dataVencimento as string,
    dataEmissao: typeof b.dataEmissao === 'string' ? b.dataEmissao : '',
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: texto(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
  }
}

function lerPendencia(dado: unknown): PendenciaColaborador {
  const b = objeto(dado, 'Pendência')
  exigir(typeof b.colaboradorId === 'number', 'Pendência')
  return {
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: texto(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
    mensagens: textos(b.mensagens),
  }
}

function lerAlertas(dado: unknown): AlertasCapacitacao {
  const b = objeto(dado, 'Alertas')
  exigir(typeof b.dataReferencia === 'string' && typeof b.diasAntecedenciaAlerta === 'number', 'Alertas')
  return {
    dataReferencia: b.dataReferencia as string,
    diasAntecedenciaAlerta: b.diasAntecedenciaAlerta as number,
    proximasDoVencimento: lista(b.proximasDoVencimento ?? [], lerAlerta, 'alertas'),
    vencidas: lista(b.vencidas ?? [], lerAlerta, 'alertas'),
    pendencias: lista(b.pendencias ?? [], lerPendencia, 'pendências'),
  }
}

function lerAptidao(dado: unknown): Aptidao {
  const b = objeto(dado, 'Aptidão')
  exigir(typeof b.apto === 'boolean' && typeof b.tarefaId === 'number' && typeof b.colaboradorId === 'number', 'Aptidão')
  return {
    apto: b.apto as boolean,
    tarefaId: b.tarefaId as number,
    tarefaDescricao: texto(b.tarefaDescricao) ?? `Tarefa #${b.tarefaId}`,
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: texto(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
    impedimentos: textos(b.impedimentos),
  }
}

export async function listarColaboradores(situacao: SituacaoCertificacao | null, signal?: AbortSignal): Promise<ResumoColaborador[]> {
  const params = situacao ? { situacao } : undefined
  const { data } = await http.get<unknown>('/capacitacoes/colaboradores', { params, signal })
  return lista(data, lerResumo, 'colaboradores')
}

export async function consultarColaborador(colaboradorId: number, signal?: AbortSignal): Promise<CapacitacoesColaborador> {
  const { data } = await http.get<unknown>(`/capacitacoes/colaboradores/${colaboradorId}`, { signal })
  return lerDetalhe(data)
}

export async function consultarAlertas(signal?: AbortSignal): Promise<AlertasCapacitacao> {
  const { data } = await http.get<unknown>('/capacitacoes/alertas', { signal })
  return lerAlertas(data)
}

export async function consultarAptidao(colaboradorId: number, tarefaId: number, signal?: AbortSignal): Promise<Aptidao> {
  const { data } = await http.get<unknown>(`/capacitacoes/colaboradores/${colaboradorId}/aptidao`,
    { params: { tarefaId }, signal })
  return lerAptidao(data)
}

/** Mensagem para falhas das consultas: perfil sem acesso, cadastro removido ou erro de rede. */
export function descreverFalha(erro: unknown, naoEncontrado: string): string {
  const status = statusDoErro(erro)
  if (status === 403) return 'Seu perfil não tem permissão para consultar capacitações.'
  if (status === 404) return mensagemDaApi(erro) ?? naoEncontrado
  if (status === 400) return mensagemDaApi(erro) ?? 'A consulta foi recusada pelo servidor. Revise os dados e tente novamente.'
  return 'Não foi possível consultar agora. Verifique sua conexão ou tente novamente em alguns instantes.'
}
