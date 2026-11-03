// Cliente tipado da API de EPIs e estoque (/api/epis): tipos Epi e Movimentacao, validação das
// respostas, consulta de catálogo e histórico, registro de entrada e saída e resolução do nome
// do operador de cada movimentação (RF03, UC06, US03, issues #123 e #133).

import { http } from '../../shared/api/https'
import { lista, numeroOuNulo, objeto, texto, textoOuNulo } from '../../shared/api/leitura'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'

export type StatusEpi = 'DISPONIVEL' | 'EM_USO' | 'EM_MANUTENCAO' | 'ESGOTADO' | 'DESCARTADO'
export type TipoMovimentacao = 'ENTRADA' | 'SAIDA'

/** Campos do contrato `EpiResponse` aprovados pela decisão #124: sem código de barras nem fornecedor. */
export type Epi = {
  id: number
  nome: string
  numeroCa: string
  quantidade: number
  estoqueMinimo: number
  /** Regra do backend (saldo <= mínimo, US03 regra 3). A tela exibe, não recalcula. */
  estoqueCritico: boolean
  status: StatusEpi
  dataValidadeCa: string | null
  descricao: string | null
  classificacao: string | null
  localizacao: string | null
}

export type Movimentacao = {
  id: number
  epiId: number
  responsavelId: number | null
  tipo: TipoMovimentacao
  quantidade: number
  dataHora: string
  motivo: string | null
  saldoAtual: number
}

/** Movimentação com o nome do operador já resolvido para exibição (UC06, passo 8; RNF05). */
export type MovimentacaoExibida = Movimentacao & { operador: string }

export type DadosMovimentacao = { tipo: TipoMovimentacao; quantidade: number; motivo: string }

const statusConhecidos: readonly StatusEpi[] = ['DISPONIVEL', 'EM_USO', 'EM_MANUTENCAO', 'ESGOTADO', 'DESCARTADO']

/** Também lê `GET /api/emprestimos/epis-disponiveis`, que devolve o mesmo `EpiResponse` (issue #110). */
export function lerEpi(dado: unknown): Epi {
  const bruto = objeto(dado, 'Registro de EPI inválido')
  if (typeof bruto.id !== 'number' || typeof bruto.nome !== 'string' || typeof bruto.quantidade !== 'number'
    || typeof bruto.estoqueMinimo !== 'number' || !statusConhecidos.includes(bruto.status as StatusEpi)) {
    throw new Error('Registro de EPI incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    nome: bruto.nome,
    numeroCa: typeof bruto.numeroCa === 'string' ? bruto.numeroCa : '',
    quantidade: bruto.quantidade,
    estoqueMinimo: bruto.estoqueMinimo,
    estoqueCritico: bruto.estoqueCritico === true,
    status: bruto.status as StatusEpi,
    dataValidadeCa: textoOuNulo(bruto.dataValidadeCa),
    descricao: textoOuNulo(bruto.descricao),
    classificacao: textoOuNulo(bruto.classificacao),
    localizacao: textoOuNulo(bruto.localizacao),
  }
}

function lerMovimentacao(dado: unknown): Movimentacao {
  const bruto = objeto(dado, 'Movimentação inválida')
  if (typeof bruto.id !== 'number' || typeof bruto.epiId !== 'number' || typeof bruto.quantidade !== 'number'
    || typeof bruto.dataHora !== 'string' || (bruto.tipo !== 'ENTRADA' && bruto.tipo !== 'SAIDA')) {
    throw new Error('Movimentação incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    epiId: bruto.epiId,
    responsavelId: numeroOuNulo(bruto.responsavelId),
    tipo: bruto.tipo,
    quantidade: bruto.quantidade,
    dataHora: bruto.dataHora,
    motivo: textoOuNulo(bruto.motivo),
    saldoAtual: typeof bruto.saldoAtual === 'number' ? bruto.saldoAtual : NaN,
  }
}

/** `GET /api/epis`: catálogo com saldo e situação crítica (Gestor e Supervisor consultam). */
export async function listarEpis(signal?: AbortSignal): Promise<Epi[]> {
  const { data } = await http.get<unknown>('/epis', { signal })
  return lista(data, lerEpi, 'Esperava uma lista de EPIs')
}

/** Exclusivo do Gestor (UC06). O backend valida quantidade, saldo e registra o operador do token. */
export async function registrarMovimentacao(epiId: number, dados: DadosMovimentacao): Promise<Movimentacao> {
  const { data } = await http.post<unknown>(`/epis/${epiId}/movimentacoes`, dados)
  return lerMovimentacao(data)
}

// A API devolve só o id do operador (issue #132). Os nomes vêm de GET /api/usuarios/{id}, uma vez por id.
const nomesDeOperadores = new Map<number, Promise<string>>()

export function nomeDoOperador(id: number, signal?: AbortSignal): Promise<string> {
  const existente = nomesDeOperadores.get(id)
  if (existente) return existente
  const consulta = http.get<unknown>(`/usuarios/${id}`, { signal })
    .then(({ data }) => {
      const nome = data !== null && typeof data === 'object' ? (data as { nome?: unknown }).nome : undefined
      return texto(nome, `Usuário ${id}`)
    })
    .catch(erro => {
      nomesDeOperadores.delete(id)
      throw erro
    })
  nomesDeOperadores.set(id, consulta)
  return consulta
}

/** `GET /api/epis/{id}/movimentacoes` com o operador de cada linha resolvido; falha de nome não derruba o histórico. */
export async function listarHistorico(epiId: number, signal?: AbortSignal): Promise<MovimentacaoExibida[]> {
  const { data } = await http.get<unknown>(`/epis/${epiId}/movimentacoes`, { signal })
  const movimentacoes = lista(data, lerMovimentacao, 'Esperava uma lista de movimentações')
  return Promise.all(movimentacoes.map(async movimentacao => ({
    ...movimentacao,
    operador: movimentacao.responsavelId === null
      ? 'Não identificado'
      : await nomeDoOperador(movimentacao.responsavelId, signal).catch(() => `Usuário ${movimentacao.responsavelId}`),
  })))
}

const movimentacaoRecusada = usarMensagemDaApi('A movimentação foi recusada pelo servidor. Confira a quantidade e o motivo.')

export const descreverFalhaDeMovimentacao = criarDescritorDeFalha({
  400: movimentacaoRecusada,
  403: 'Seu perfil não tem permissão para movimentar o estoque. Apenas o Gestor de Segurança registra entradas e saídas.',
  404: 'EPI não encontrado. Ele pode ter sido alterado por outra pessoa; recarregue a lista.',
},'Não foi possível registrar a movimentação agora. Os dados preenchidos foram mantidos; tente novamente.')
