// Validação local do registro de inspeção (US09, issue #136), igual à da API: data obrigatória e não
// posterior a hoje, ao menos um item, e cada item com referência avaliada e resultado.

import type { DadosRegistroInspecao } from './apiInspecoes'

export type Resultado = '' | 'conforme' | 'nao-conforme'

export type RascunhoItem = { chave: number; descricao: string; resultado: Resultado; observacoes: string }

export type ErrosItem = { descricao?: string; resultado?: string }

export type ErrosInspecao = { dataInspecao?: string; itens: Record<number, ErrosItem> }

/** Data local de hoje em `AAAA-MM-DD`, o mesmo formato do campo de data e da API. */
export function hojeIso(agora: Date = new Date()): string {
  const doisDigitos = (valor: number) => String(valor).padStart(2, '0')
  return `${agora.getFullYear()}-${doisDigitos(agora.getMonth() + 1)}-${doisDigitos(agora.getDate())}`
}

function validarItem(item: RascunhoItem): ErrosItem {
  const erros: ErrosItem = {}
  if (!item.descricao.trim()) erros.descricao = 'Informe a norma ou referência avaliada.'
  if (!item.resultado) erros.resultado = 'Indique conformidade ou não conformidade.'
  return erros
}

export function validarInspecao(dataInspecao: string, itens: RascunhoItem[], hoje: string): {
  dados?: DadosRegistroInspecao
  erros: ErrosInspecao
} {
  const erros: ErrosInspecao = { itens: {} }
  if (!dataInspecao) erros.dataInspecao = 'Informe a data em que a inspeção foi realizada.'
  else if (dataInspecao > hoje) erros.dataInspecao = 'A data da inspeção não pode ser posterior a hoje.'
  for (const item of itens) {
    const errosItem = validarItem(item)
    if (errosItem.descricao || errosItem.resultado) erros.itens[item.chave] = errosItem
  }
  if (erros.dataInspecao || Object.keys(erros.itens).length > 0 || itens.length === 0) return { erros }
  return {
    erros,
    dados: {
      dataInspecao,
      itens: itens.map(item => ({
        descricao: item.descricao.trim(),
        conformidade: item.resultado === 'conforme',
        ...(item.observacoes.trim() ? { observacoes: item.observacoes.trim() } : {}),
      })),
    },
  }
}
