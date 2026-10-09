// Funções puras de apresentação das capacitações: rótulo e tom da situação da certificação, textos
// da ausência de certificações e da antecedência do alerta.

import type { FeedbackTone } from '../../shared/components'
import type { ResumoColaborador, SituacaoCertificacao, SituacaoTreinamento } from './api'

export const rotuloDaSituacao: Record<SituacaoCertificacao, string> = {
  EM_DIA: 'Em dia',
  PROXIMA_DO_VENCIMENTO: 'Próxima do vencimento',
  VENCIDA: 'Vencida',
}

export const tomDaSituacao: Record<SituacaoCertificacao, FeedbackTone> = {
  EM_DIA: 'success',
  PROXIMA_DO_VENCIMENTO: 'warning',
  VENCIDA: 'danger',
}

export const rotuloDaSituacaoTreinamento: Record<SituacaoTreinamento, string> = {
  PENDENTE: 'Pendente',
  REALIZADO: 'Realizado',
  PROXIMO_DO_VENCIMENTO: 'Próximo do vencimento',
  VENCIDO: 'Vencido',
}

export const tomDaSituacaoTreinamento: Record<SituacaoTreinamento, FeedbackTone> = {
  PENDENTE: 'warning',
  REALIZADO: 'success',
  PROXIMO_DO_VENCIMENTO: 'warning',
  VENCIDO: 'danger',
}

/** Rótulo das opções do filtro (UC02, cenário alternativo IV), com o prazo do alerta. */
export function rotuloDoFiltro(situacao: SituacaoCertificacao, dias = 30): string {
  if (situacao === 'PROXIMA_DO_VENCIMENTO') return `Próximas do vencimento (até ${dias} dias)`
  if (situacao === 'VENCIDA') return 'Vencidas'
  return 'Em dia'
}

export const textoSemCertificacoes = 'Sem certificações cadastradas'

/** Situação exibida na listagem: a mais crítica entre as certificações ou a ausência delas. */
export function situacaoDoResumo(resumo: Pick<ResumoColaborador, 'situacaoCertificacoes'>): { rotulo: string; tom: FeedbackTone } {
  return resumo.situacaoCertificacoes
    ? { rotulo: rotuloDaSituacao[resumo.situacaoCertificacoes], tom: tomDaSituacao[resumo.situacaoCertificacoes] }
    : { rotulo: textoSemCertificacoes, tom: 'info' }
}

export function plural(quantidade: number, singular: string, pluralTexto: string): string {
  return `${quantidade} ${quantidade === 1 ? singular : pluralTexto}`
}
