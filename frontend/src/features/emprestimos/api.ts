// Cliente tipado da API de empréstimos (/api/emprestimos; RF11, UC12, US11, issue #110): tipos
// Emprestimo e DadosEntrega, validação das respostas, consulta de posse, pendências e atrasos,
// entrega, devolução, nomes dos responsáveis e leitura das recusas do servidor.

import { http } from '../../shared/api/https'
import { criarDescritorDeFalha, mensagemDaApi, statusDoErro, usarMensagemDaApi } from '../../shared/api/erros'
import { lerEpi, nomeDoOperador, type Epi } from '../estoque/api'
import { listarColaboradores, type Usuario } from '../usuarios/api'

/** Valores do diagrama de classes, lidos do ponto de vista do EPI dentro do empréstimo (issue #106). */
export type ClassificacaoEmprestimo = 'INDISPONIVEL' | 'DEVOLUCAO_ATRASADA' | 'DISPONIVEL'
export type EstadoDevolucao = 'APTO' | 'MANUTENCAO' | 'DESCARTE'
/** Filtro `situacao` de `GET /api/emprestimos` (US11, critérios 2 e 3). */
export type SituacaoConsulta = 'PENDENTES' | 'ATRASADOS' | 'TODOS'

/** Campos de `EmprestimoResponse`. Classificação e atraso vêm calculados pelo backend. */
export type Emprestimo = {
  id: number
  epiId: number
  epiNome: string
  colaboradorId: number
  colaboradorNome: string
  responsavelEntregaId: number | null
  dataRetirada: string
  dataDevolucaoPrevista: string
  dataDevolucaoReal: string | null
  responsavelDevolucaoId: number | null
  estadoDevolucao: EstadoDevolucao | null
  status: ClassificacaoEmprestimo
  atrasado: boolean
  termoCautela: string
  justificativaSubstituicao: string | null
}

/** Corpo de `POST /api/emprestimos`. O operador vem do token e nunca vai no corpo. */
export type DadosEntrega = {
  epiId: number
  colaboradorId: number
  dataDevolucaoPrevista: string
  confirmarSubstituicao?: boolean
  justificativaSubstituicao?: string
}

export type Responsaveis = { entrega: string; devolucao: string | null }

export const estadosDeDevolucao: readonly EstadoDevolucao[] = ['APTO', 'MANUTENCAO', 'DESCARTE']
const classificacoes: readonly ClassificacaoEmprestimo[] = ['INDISPONIVEL', 'DEVOLUCAO_ATRASADA', 'DISPONIVEL']

function textoOuNulo(valor: unknown): string | null {
  return typeof valor === 'string' && valor.trim() ? valor : null
}

function numeroOuNulo(valor: unknown): number | null {
  return typeof valor === 'number' ? valor : null
}

function estadoOuNulo(valor: unknown): EstadoDevolucao | null {
  return estadosDeDevolucao.includes(valor as EstadoDevolucao) ? valor as EstadoDevolucao : null
}

function lerEmprestimo(dado: unknown): Emprestimo {
  if (dado === null || typeof dado !== 'object') throw new Error('Registro de empréstimo inválido')
  const bruto = dado as Record<string, unknown>
  if (typeof bruto.id !== 'number' || typeof bruto.epiId !== 'number' || typeof bruto.colaboradorId !== 'number'
    || typeof bruto.dataRetirada !== 'string' || typeof bruto.dataDevolucaoPrevista !== 'string'
    || !classificacoes.includes(bruto.status as ClassificacaoEmprestimo)) {
    throw new Error('Registro de empréstimo incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    epiId: bruto.epiId,
    epiNome: textoOuNulo(bruto.epiNome) ?? `EPI ${bruto.epiId}`,
    colaboradorId: bruto.colaboradorId,
    colaboradorNome: textoOuNulo(bruto.colaboradorNome) ?? `Colaborador ${bruto.colaboradorId}`,
    responsavelEntregaId: numeroOuNulo(bruto.responsavelEntregaId),
    dataRetirada: bruto.dataRetirada,
    dataDevolucaoPrevista: bruto.dataDevolucaoPrevista,
    dataDevolucaoReal: textoOuNulo(bruto.dataDevolucaoReal),
    responsavelDevolucaoId: numeroOuNulo(bruto.responsavelDevolucaoId),
    estadoDevolucao: estadoOuNulo(bruto.estadoDevolucao),
    status: bruto.status as ClassificacaoEmprestimo,
    atrasado: bruto.atrasado === true,
    termoCautela: typeof bruto.termoCautela === 'string' ? bruto.termoCautela : '',
    justificativaSubstituicao: textoOuNulo(bruto.justificativaSubstituicao),
  }
}

function lerLista<T>(dados: unknown, ler: (dado: unknown) => T, descricao: string): T[] {
  if (!Array.isArray(dados)) throw new Error(`Esperava uma lista de ${descricao}`)
  return dados.map(ler)
}

/** `GET /api/emprestimos?situacao=...`: posse, pendências e atrasos (Supervisor e Gestor). */
export async function listarEmprestimos(situacao: SituacaoConsulta, signal?: AbortSignal): Promise<Emprestimo[]> {
  const { data } = await http.get<unknown>('/emprestimos', { params: { situacao }, signal })
  return lerLista(data, lerEmprestimo, 'empréstimos')
}

/** Passo 4 do UC12: EPIs na situação Disponível, com saldo e CA válido, filtrados pelo backend. */
export async function listarEpisDisponiveis(signal?: AbortSignal): Promise<Epi[]> {
  const { data } = await http.get<unknown>('/emprestimos/epis-disponiveis', { signal })
  return lerLista(data, lerEpi, 'EPIs disponíveis')
}

/** Só cadastros ativos podem receber EPI: o backend recusa a entrega a colaborador inativo. */
export async function listarColaboradoresAtivos(signal?: AbortSignal): Promise<Usuario[]> {
  const colaboradores = await listarColaboradores({}, signal)
  return colaboradores.filter(colaborador => colaborador.ativo)
    .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
}

export async function registrarEntrega(dados: DadosEntrega): Promise<Emprestimo> {
  const { data } = await http.post<unknown>('/emprestimos', dados)
  return lerEmprestimo(data)
}

function lerDevolucao(dado: unknown): Emprestimo {
  if (dado === null || typeof dado !== 'object') throw new Error('Registro de devolução inválido')
  const bruto = dado as Record<string, unknown>
  const id = typeof bruto.emprestimoId === 'number'
    ? bruto.emprestimoId
    : typeof bruto.id === 'number' ? bruto.id : 0
  return {
    id,
    epiId: typeof bruto.epiId === 'number' ? bruto.epiId : 0,
    epiNome: textoOuNulo(bruto.epiNome) ?? `EPI ${bruto.epiId}`,
    colaboradorId: typeof bruto.colaboradorId === 'number' ? bruto.colaboradorId : 0,
    colaboradorNome: textoOuNulo(bruto.colaboradorNome) ?? `Colaborador ${bruto.colaboradorId}`,
    responsavelEntregaId: numeroOuNulo(bruto.responsavelEntregaId),
    dataRetirada: typeof bruto.dataRetirada === 'string' ? bruto.dataRetirada : '',
    dataDevolucaoPrevista: typeof bruto.dataDevolucaoPrevista === 'string' ? bruto.dataDevolucaoPrevista : '',
    dataDevolucaoReal: textoOuNulo(bruto.dataDevolucaoReal),
    responsavelDevolucaoId: numeroOuNulo(bruto.responsavelDevolucaoId),
    estadoDevolucao: estadoOuNulo(bruto.estadoDevolucao),
    status: 'DISPONIVEL',
    atrasado: false,
    termoCautela: typeof bruto.termoCautela === 'string' ? bruto.termoCautela : '',
    justificativaSubstituicao: textoOuNulo(bruto.justificativaSubstituicao),
  }
}

/** Só `APTO` volta ao saldo; `MANUTENCAO` e `DESCARTE` encerram o empréstimo sem creditar o estoque. */
export async function registrarDevolucao(id: number, estado: EstadoDevolucao): Promise<Emprestimo> {
  const { data } = await http.post<unknown>(`/emprestimos/${id}/devolucao`, { estado })
  return lerDevolucao(data)
}

// Sem AbortSignal de propósito: `nomeDoOperador` guarda a promessa por id, e uma consulta cancelada
// (por exemplo, pelo efeito duplicado do StrictMode) ficaria no cache e viraria "Usuário N".
function nomeOuReserva(id: number | null): Promise<string | null> {
  if (id === null) return Promise.resolve(null)
  return nomeDoOperador(id).catch(() => `Usuário ${id}`)
}

/** A API devolve só os ids dos responsáveis (UC12, regra 2); os nomes vêm de `GET /api/usuarios/{id}`. */
export async function nomesDosResponsaveis(emprestimo: Emprestimo): Promise<Responsaveis> {
  const [entrega, devolucao] = await Promise.all([
    nomeOuReserva(emprestimo.responsavelEntregaId),
    nomeOuReserva(emprestimo.responsavelDevolucaoId),
  ])
  return { entrega: entrega ?? 'Não identificado', devolucao }
}

/** 409 da entrega: o colaborador já tem EPI do mesmo tipo em posse (UC12, exceção III). */
export function duplicidadeDaFalha(erro: unknown): string | null {
  if (statusDoErro(erro) !== 409) return null
  return mensagemDaApi(erro)
    ?? 'O colaborador já possui um EPI do mesmo tipo em posse. Confirme a substituição e informe a justificativa.'
}

const falhaEntrega = usarMensagemDaApi('A entrega foi recusada pelo servidor. Confira o colaborador, o EPI e a data.')

/** 422 traz o impedimento do UC12 (saldo, CA, situação do EPI, capacitação ou colaborador inativo). */
export const descreverFalhaDeEntrega = criarDescritorDeFalha({
  400: falhaEntrega,
  403: 'Seu perfil não tem permissão para registrar entregas de EPI.',
  404: falhaEntrega,
  422: falhaEntrega,
}, 'Não foi possível registrar a entrega agora. Os dados preenchidos foram mantidos; tente novamente.')

const emprestimoInativo = usarMensagemDaApi('Este empréstimo não está mais ativo. Feche o diálogo e recarregue a lista.')
const devolucaoRecusada = usarMensagemDaApi('A devolução foi recusada pelo servidor. Confira a condição informada.')

export const descreverFalhaDeDevolucao = criarDescritorDeFalha({
  400: devolucaoRecusada,
  403: 'Seu perfil não tem permissão para registrar devoluções.',
  404: emprestimoInativo,
  409: emprestimoInativo,
}, 'Não foi possível registrar a devolução agora. A condição escolhida foi mantida; tente novamente.')