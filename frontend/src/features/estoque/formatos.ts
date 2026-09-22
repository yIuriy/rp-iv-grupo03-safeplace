// Funções puras de apresentação da tela de EPIs: rótulos de status, classificação e tipo de
// movimentação, e formatação de data e de data/hora vindas da API.

import type { StatusEpi, TipoMovimentacao } from './api'

export const rotuloDoStatus: Record<StatusEpi, string> = {
  DISPONIVEL: 'Disponível',
  EM_USO: 'Em uso',
  EM_MANUTENCAO: 'Em manutenção',
  ESGOTADO: 'Esgotado',
  DESCARTADO: 'Descartado',
}

export const rotuloDoTipo: Record<TipoMovimentacao, string> = { ENTRADA: 'Entrada', SAIDA: 'Saída' }

/** Os nove valores de `ClassificacaoEPI` do diagrama de classes, com acentuação para exibição. */
const rotulosDeClassificacao: Record<string, string> = {
  PROTECAO_CONTRA_QUEDAS: 'Proteção contra quedas',
  PROTECAO_DE_MEMBROS_INFERIORES: 'Proteção de membros inferiores',
  PROTECAO_DE_MEMBROS_SUPERIORES: 'Proteção de membros superiores',
  PROTECAO_DE_TRONCO: 'Proteção de tronco',
  PROTECAO_RESPIRATORIA: 'Proteção respiratória',
  PROTECAO_AUDITIVA: 'Proteção auditiva',
  PROTECAO_DE_OLHOS: 'Proteção de olhos',
  PROTECAO_DE_FACE: 'Proteção de face',
  PROTECAO_DE_CABECA: 'Proteção de cabeça',
}

/** Valores fora da lista aparecem legíveis (sublinhados virando espaços), sem esconder o dado. */
export function rotuloDaClassificacao(valor: string | null): string {
  if (!valor) return 'Não informada'
  const conhecido = rotulosDeClassificacao[valor]
  if (conhecido) return conhecido
  const texto = valor.toLowerCase().replaceAll('_', ' ')
  return texto.charAt(0).toUpperCase() + texto.slice(1)
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
