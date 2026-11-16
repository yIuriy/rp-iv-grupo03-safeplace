// Funções puras de apresentação da tela de EPIs: rótulos de status, classificação e tipo de
// movimentação, e formatação de data e de data/hora vindas da API.

import { rotuloClassificacaoEpi, type ClassificacaoEpi, type StatusEpi } from '../epis/epi'
import type { TipoMovimentacao } from './api'

export const rotuloDoStatus: Record<StatusEpi, string> = {
  DISPONIVEL: 'Disponível',
  EM_USO: 'Em uso',
  EM_MANUTENCAO: 'Em manutenção',
  ESGOTADO: 'Esgotado',
  DESCARTADO: 'Descartado',
}

export const rotuloDoTipo: Record<TipoMovimentacao, string> = { ENTRADA: 'Entrada', SAIDA: 'Saída' }

/** O adaptador `lerEpi` já troca valores fora do enum por `null`; os rótulos vêm do contrato único. */
export function rotuloDaClassificacao(valor: ClassificacaoEpi | null): string {
  return valor ? rotuloClassificacaoEpi[valor] : 'Não informada'
}

