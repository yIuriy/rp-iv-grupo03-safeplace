// Funções puras de apresentação da tela de empréstimos: situação textual do empréstimo, condição e
// destino na devolução, rótulos de colaborador, EPI e alertas de reposição e o texto dos dias
// restantes. Datas usam os formatadores da tela de estoque.

import type { FeedbackTone } from '../../shared/components'
import type { Epi } from '../estoque/api'
import type { Usuario } from '../usuarios/api'
import { ocultarCpf } from '../usuarios/formatos'
import type { Emprestimo, EstadoDevolucao } from './api'

export type SituacaoExibida = { rotulo: string; tom: FeedbackTone }

/** O texto acompanha sempre a cor do selo (RNF11); atraso vem do backend (US11, regra 6). */
export function situacaoDoEmprestimo(emprestimo: Emprestimo): SituacaoExibida {
  if (emprestimo.status === 'DISPONIVEL') return { rotulo: 'Devolvido', tom: 'success' }
  if (emprestimo.atrasado || emprestimo.status === 'DEVOLUCAO_ATRASADA') return { rotulo: 'Atrasado', tom: 'danger' }
  return { rotulo: 'Em posse', tom: 'info' }
}

/** Opções da condição do item devolvido (UC12, alternativo I; dicionário da US11). */
export const opcaoDoEstado: Record<EstadoDevolucao, string> = {
  APTO: 'Apto para uso',
  MANUTENCAO: 'Encaminhar para manutenção',
  DESCARTE: 'Indicar descarte',
}

export const rotuloDoEstado: Record<EstadoDevolucao, string> = {
  APTO: 'Apto para uso',
  MANUTENCAO: 'Indicado para manutenção',
  DESCARTE: 'Indicado para descarte',
}

/** Destino confirmado antes do envio: só o item apto volta ao saldo (US11, regra 5; RF14 no backlog). */
export const destinoDoEstado: Record<EstadoDevolucao, string> = {
  APTO: 'O empréstimo será encerrado e o item voltará ao saldo disponível do estoque.',
  MANUTENCAO: 'O empréstimo será encerrado com a indicação de manutenção. O item não voltará ao saldo disponível.',
  DESCARTE: 'O empréstimo será encerrado com a indicação de descarte. O item não voltará ao saldo disponível, e a baixa definitiva não é registrada nesta tela.',
}

export function mensagemDeDevolucao(emprestimo: Emprestimo): string {
  if (emprestimo.estadoDevolucao === 'APTO') return `Devolução registrada: ${emprestimo.epiNome} voltou ao saldo disponível.`
  const destino = emprestimo.estadoDevolucao === 'DESCARTE' ? 'descarte' : 'manutenção'
  return `Devolução registrada: ${emprestimo.epiNome} indicado para ${destino}, fora do saldo disponível.`
}

/** O CPF aparece coberto no início e no fim, como na tela de usuários, para distinguir homônimos. */
export function rotuloDoColaborador(colaborador: Usuario): string {
  return `${colaborador.nome} · CPF ${ocultarCpf(colaborador.cpf)}`
}

export function rotuloDoEpi(epi: Epi): string {
  return `${epi.nome} · código ${epi.id} · CA ${epi.numeroCa || 'não informado'} · saldo ${epi.quantidade}`
}

const rotulosDeAlerta: Record<string, string> = {
  DESCARTE_PROXIMO: 'Descarte próximo',
  CA_PROXIMO_VENCIMENTO: 'CA perto do vencimento',
  ESTOQUE_CRITICO: 'Estoque crítico',
}

/** Tipos fora da lista aparecem legíveis, sem esconder o dado. */
export function rotuloDoAlerta(tipo: string): string {
  const conhecido = rotulosDeAlerta[tipo]
  if (conhecido) return conhecido
  const legivel = tipo.toLowerCase().replaceAll('_', ' ')
  return legivel.charAt(0).toUpperCase() + legivel.slice(1)
}

export function textoDosDiasRestantes(dias: number | null): string {
  if (dias === null) return 'Não se aplica'
  if (dias === 0) return 'Hoje'
  if (dias < 0) return dias === -1 ? 'Vencido há 1 dia' : `Vencido há ${-dias} dias`
  return dias === 1 ? '1 dia' : `${dias} dias`
}
