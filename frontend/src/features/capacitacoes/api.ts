// Cliente tipado da API de capacitações (/api/capacitacoes, issues #119 e #192): listagem com filtro
// por situação, detalhes do colaborador, alertas de vencimento, consulta de aptidão para uma tarefa,
// cadastro de certificações, treinamentos e exigências, e tentativa de alocação (RF10, UC02, US10).

import { isAxiosError } from 'axios'
import { http } from '../../shared/api/https'
import { lista, numeroOuNulo, objeto, textoOuNulo, type Bruto } from '../../shared/api/leitura'
import { criarDescritorDeFalha, mensagemDaApi, statusDoErro, usarMensagemDaApi } from '../../shared/api/erros'

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

/** Situação de `Treinamento.status`; os dois últimos valores só existem para treinamento com validade. */
export type SituacaoTreinamento = 'PENDENTE' | 'REALIZADO' | 'PROXIMO_DO_VENCIMENTO' | 'VENCIDO'

const situacoesTreinamento: readonly SituacaoTreinamento[] = ['PENDENTE', 'REALIZADO', 'PROXIMO_DO_VENCIMENTO', 'VENCIDO']

/** Atributos de `Treinamento` no diagrama de classes, mais a validade opcional em meses (#192). */
export type Treinamento = {
  id: number
  nome: string
  conteudo: string | null
  instrutor: string | null
  dataRealizacao: string
  cargaHoraria: number | null
  /** Sem validade o treinamento não vence. */
  validadeMeses: number | null
  dataVencimento: string | null
  status: SituacaoTreinamento
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

export type AlertaTreinamento = {
  treinamentoId: number
  nome: string
  status: SituacaoTreinamento
  dataRealizacao: string
  dataVencimento: string
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
  treinamentosProximosDoVencimento: AlertaTreinamento[]
  treinamentosVencidos: AlertaTreinamento[]
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
  const b = objeto(dado, 'Colaborador inválido na resposta da API')
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
  const b = objeto(dado, 'Certificação inválido na resposta da API')
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
  const b = objeto(dado, 'Treinamento inválido na resposta da API')
  exigir(typeof b.id === 'number' && typeof b.nome === 'string' && typeof b.dataRealizacao === 'string', 'Treinamento')
  return {
    id: b.id as number,
    nome: b.nome as string,
    conteudo: textoOuNulo(b.conteudo),
    instrutor: textoOuNulo(b.instrutor),
    dataRealizacao: b.dataRealizacao as string,
    cargaHoraria: numeroOuNulo(b.cargaHoraria),
    validadeMeses: numeroOuNulo(b.validadeMeses),
    dataVencimento: textoOuNulo(b.dataVencimento),
    status: situacoesTreinamento.includes(b.status as SituacaoTreinamento) ? b.status as SituacaoTreinamento : 'REALIZADO',
  }
}

function lerDetalhe(dado: unknown): CapacitacoesColaborador {
  const b = objeto(dado, 'Capacitações do colaborador inválido na resposta da API')
  exigir(typeof b.colaboradorId === 'number' && typeof b.colaboradorNome === 'string', 'Capacitações do colaborador')
  return {
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: b.colaboradorNome as string,
    situacaoCertificacoes: situacaoOuNula(b.situacaoCertificacoes),
    certificacoes: lista(b.certificacoes ?? [], lerCertificacao, 'Esperava uma lista de certificações'),
    treinamentos: lista(b.treinamentos ?? [], lerTreinamento, 'Esperava uma lista de treinamentos'),
    certificacoesVencidas: lista(b.certificacoesVencidas ?? [], lerCertificacao, 'Esperava uma lista de certificações vencidas'),
    certificacoesProximasDoVencimento: lista(b.certificacoesProximasDoVencimento ?? [], lerCertificacao,
      'Esperava uma lista de certificações próximas do vencimento'),
    pendenteIntegracaoAdmissional: b.pendenteIntegracaoAdmissional === true,
    pendencias: textos(b.pendencias),
  }
}

function lerAlerta(dado: unknown): AlertaCertificacao {
  const b = objeto(dado, 'Alerta de certificação inválido na resposta da API')
  exigir(typeof b.certificacaoId === 'number' && typeof b.nome === 'string' && ehSituacao(b.status)
    && typeof b.dataVencimento === 'string' && typeof b.colaboradorId === 'number', 'Alerta de certificação')
  return {
    certificacaoId: b.certificacaoId as number,
    nome: b.nome as string,
    status: b.status as SituacaoCertificacao,
    dataVencimento: b.dataVencimento as string,
    dataEmissao: typeof b.dataEmissao === 'string' ? b.dataEmissao : '',
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: textoOuNulo(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
  }
}

function lerAlertaTreinamento(dado: unknown): AlertaTreinamento {
  const b = objeto(dado, 'Alerta de treinamento inválido na resposta da API')
  exigir(typeof b.treinamentoId === 'number' && typeof b.nome === 'string'
    && situacoesTreinamento.includes(b.status as SituacaoTreinamento)
    && typeof b.dataVencimento === 'string' && typeof b.colaboradorId === 'number', 'Alerta de treinamento')
  return {
    treinamentoId: b.treinamentoId as number,
    nome: b.nome as string,
    status: b.status as SituacaoTreinamento,
    dataRealizacao: typeof b.dataRealizacao === 'string' ? b.dataRealizacao : '',
    dataVencimento: b.dataVencimento as string,
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: textoOuNulo(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
  }
}

function lerPendencia(dado: unknown): PendenciaColaborador {
  const b = objeto(dado, 'Pendência inválido na resposta da API')
  exigir(typeof b.colaboradorId === 'number', 'Pendência')
  return {
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: textoOuNulo(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
    mensagens: textos(b.mensagens),
  }
}

function lerAlertas(dado: unknown): AlertasCapacitacao {
  const b = objeto(dado, 'Alertas inválido na resposta da API')
  exigir(typeof b.dataReferencia === 'string' && typeof b.diasAntecedenciaAlerta === 'number', 'Alertas')
  return {
    dataReferencia: b.dataReferencia as string,
    diasAntecedenciaAlerta: b.diasAntecedenciaAlerta as number,
    proximasDoVencimento: lista(b.proximasDoVencimento ?? [], lerAlerta, 'Esperava uma lista de alertas'),
    vencidas: lista(b.vencidas ?? [], lerAlerta, 'Esperava uma lista de alertas'),
    pendencias: lista(b.pendencias ?? [], lerPendencia, 'Esperava uma lista de pendências'),
    treinamentosProximosDoVencimento: lista(b.treinamentosProximosDoVencimento ?? [], lerAlertaTreinamento, 'Esperava uma lista de alertas de treinamento'),
    treinamentosVencidos: lista(b.treinamentosVencidos ?? [], lerAlertaTreinamento, 'Esperava uma lista de alertas de treinamento'),
  }
}

function lerAptidao(dado: unknown): Aptidao {
  const b = objeto(dado, 'Aptidão inválido na resposta da API')
  exigir(typeof b.apto === 'boolean' && typeof b.tarefaId === 'number' && typeof b.colaboradorId === 'number', 'Aptidão')
  return {
    apto: b.apto as boolean,
    tarefaId: b.tarefaId as number,
    tarefaDescricao: textoOuNulo(b.tarefaDescricao) ?? `Tarefa #${b.tarefaId}`,
    colaboradorId: b.colaboradorId as number,
    colaboradorNome: textoOuNulo(b.colaboradorNome) ?? `Colaborador #${b.colaboradorId}`,
    impedimentos: textos(b.impedimentos),
  }
}

export async function listarColaboradores(situacao: SituacaoCertificacao | null, signal?: AbortSignal): Promise<ResumoColaborador[]> {
  const params = situacao ? { situacao } : undefined
  const { data } = await http.get<unknown>('/capacitacoes/colaboradores', { params, signal })
  return lista(data, lerResumo, 'Esperava uma lista de colaboradores')
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
  // O texto do 404 vem de quem chama, então o descritor é montado e executado a cada chamada.
  return criarDescritorDeFalha({
    400: usarMensagemDaApi('A consulta foi recusada pelo servidor. Revise os dados e tente novamente.'),
    403: 'Seu perfil não tem permissão para consultar capacitações.',
    404: usarMensagemDaApi(naoEncontrado),
  }, 'Não foi possível consultar agora. Verifique sua conexão ou tente novamente em alguns instantes.')(erro)
}

export type DadosCertificacao = { nome: string; dataEmissao: string; dataVencimento: string; colaboradorId: number }

export type DadosTreinamento = {
  nome: string
  conteudo?: string
  instrutor?: string
  dataRealizacao: string
  cargaHoraria?: number
  validadeMeses?: number
  colaboradoresIds: number[]
}

export type TipoCapacitacao = 'CERTIFICACAO' | 'TREINAMENTO'
export type AlvoExigencia = 'TAREFA' | 'EPI'

export type DadosExigencia = { nomeCapacitacao: string; tipoCapacitacao: TipoCapacitacao; alvo: AlvoExigencia; alvoId: number }

export async function cadastrarCertificacao(dados: DadosCertificacao): Promise<Certificacao> {
  const { data } = await http.post<unknown>('/capacitacoes/certificacoes', dados)
  return lerCertificacao(data)
}

export async function cadastrarTreinamento(dados: DadosTreinamento): Promise<Treinamento> {
  const { data } = await http.post<unknown>('/capacitacoes/treinamentos', dados)
  return lerTreinamento(data)
}

export async function definirExigencia(dados: DadosExigencia): Promise<void> {
  await http.post('/capacitacoes/exigencias', dados)
}

/** Resultado da tentativa de alocação: a API valida de novo as exigências no momento da confirmação. */
export type ResultadoAlocacao =
  | { alocada: true }
  | { alocada: false; titulo: string; mensagem: string; impedimentos: string[] }

export async function alocarColaborador(tarefaId: number, colaboradorId: number): Promise<ResultadoAlocacao> {
  try {
    await http.post(`/tarefas/${tarefaId}/alocacoes`, { colaboradorId })
    return { alocada: true }
  } catch (erro) {
    if (statusDoErro(erro) !== 422) throw erro
    const corpo: unknown = isAxiosError(erro) ? erro.response?.data : undefined
    const impedimentos = corpo !== null && typeof corpo === 'object' ? textos((corpo as Bruto).impedimentos) : []
    const colaboradorNaoAlocavel = corpo !== null && typeof corpo === 'object' && (corpo as Bruto).error === 'Colaborador Não Alocável'
    return {
      alocada: false,
      titulo: colaboradorNaoAlocavel ? 'Esta pessoa não pode ser alocada' : 'Alocação impedida por capacitação',
      mensagem: mensagemDaApi(erro) ?? 'O servidor recusou a alocação.',
      impedimentos,
    }
  }
}

/** Mensagem para falhas do cadastro: perfil sem acesso, dados recusados ou erro de rede. */
export const descreverFalhaCadastro = criarDescritorDeFalha({
  400: usarMensagemDaApi('O servidor recusou os dados. Revise os campos e tente novamente.'),
  403: 'Seu perfil não tem permissão para cadastrar capacitações.',
}, 'O servidor não confirmou o cadastro. Verifique a conexão e tente novamente.')
