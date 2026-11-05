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

/** Converte `AAAA-MM-DD` da API para `DD/MM/AAAA`; devolve o original se o formato for outro. */
export function formatarData(iso: string): string {
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso
}

/** Converte `AAAA-MM-DDTHH:MM[:SS]` para `DD/MM/AAAA HH:MM`, sem depender do fuso do navegador. */
export function formatarDataHora(iso: string): string {
  const [data, hora] = iso.split('T')
  if (!data || !hora) return iso
  return `${formatarData(data)} ${hora.slice(0, 5)}`
}
