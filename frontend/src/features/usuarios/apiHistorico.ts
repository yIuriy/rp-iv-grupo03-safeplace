// Chamada do histórico do próprio perfil (RNF05): GET /api/usuarios/me/historico devolve as ações que
// a pessoa logada registrou, lidas da trilha de auditoria, da mais recente para a mais antiga.

import { http } from '../../shared/api/https'
import { lista, numeroOuNulo, objeto, textoOuNulo } from '../../shared/api/leitura'

export type TipoOperacao = 'CADASTRO' | 'ALTERACAO' | 'REMOCAO'

export type AcaoUsuario = {
  id: number
  tipoOperacao: string
  dataHora: string
  entidade: string | null
  entidadeId: number | null
  descricao: string | null
}

export const LIMITE_HISTORICO = 20

function lerAcao(dado: unknown): AcaoUsuario {
  const b = objeto(dado, 'Ação inválida na resposta')
  if (typeof b.id !== 'number' || typeof b.tipoOperacao !== 'string' || typeof b.dataHora !== 'string') {
    throw new Error('Ação inválida na resposta')
  }
  return {
    id: b.id,
    tipoOperacao: b.tipoOperacao,
    dataHora: b.dataHora,
    entidade: textoOuNulo(b.entidade),
    entidadeId: numeroOuNulo(b.entidadeId),
    descricao: textoOuNulo(b.descricao),
  }
}

export async function listarMinhasAcoes(signal?: AbortSignal): Promise<AcaoUsuario[]> {
  const { data } = await http.get<unknown>('/usuarios/me/historico', { params: { limite: LIMITE_HISTORICO }, signal })
  return lista(data, lerAcao, 'Resposta inválida do histórico')
}

/** Nomes gravados no log pelo backend, em português para a tela; nome desconhecido aparece como veio. */
const rotuloEntidade: Record<string, string> = {
  Acidente: 'Acidente',
  AlocacaoTarefa: 'Alocação em tarefa',
  AreaRisco: 'Área de risco',
  Certificacao: 'Certificação',
  Colaborador: 'Colaborador',
  Emprestimo: 'Empréstimo de EPI',
  Epi: 'EPI',
  Incidente: 'Incidente',
  Inspecao: 'Inspeção',
  ManutencaoEpi: 'Manutenção de EPI',
  PlanoDeAcao: 'Plano de ação',
  Supervisor: 'Supervisor',
  Tarefa: 'Tarefa',
  Treinamento: 'Treinamento',
  Usuario: 'Usuário',
}

export function descreverRegistro(acao: AcaoUsuario): string {
  const nome = acao.entidade ? (rotuloEntidade[acao.entidade] ?? acao.entidade) : 'Registro'
  return acao.entidadeId === null ? nome : `${nome} ${acao.entidadeId}`
}

export const rotuloOperacao: Record<TipoOperacao, string> = {
  CADASTRO: 'Cadastro',
  ALTERACAO: 'Alteração',
  REMOCAO: 'Remoção',
}

export const tomOperacao: Record<TipoOperacao, 'success' | 'info' | 'danger'> = {
  CADASTRO: 'success',
  ALTERACAO: 'info',
  REMOCAO: 'danger',
}
