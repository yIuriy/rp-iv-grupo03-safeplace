// Leitura e validação local do formulário de movimentação de estoque (US03): quantidade inteira
// maior que zero, saída limitada ao saldo conhecido e motivo obrigatório. A regra definitiva
// (saldo nunca negativo) é do backend; aqui só se evita uma chamada que já se sabe inválida.

import type { DadosMovimentacao } from './api'

export type ErrosMovimentacao = Partial<Record<'quantidade' | 'motivo', string>>

export const ordemDosCamposDeMovimentacao = ['quantidade', 'motivo'] as const

export function lerDadosMovimentacao(form: FormData): DadosMovimentacao {
  const tipo = form.get('tipo') === 'SAIDA' ? 'SAIDA' : 'ENTRADA'
  const quantidade = Number(String(form.get('quantidade') ?? '').trim())
  return { tipo, quantidade, motivo: String(form.get('motivo') ?? '').trim() }
}

export function validarMovimentacao(dados: DadosMovimentacao, saldo: number): ErrosMovimentacao {
  const erros: ErrosMovimentacao = {}
  if (!Number.isInteger(dados.quantidade) || dados.quantidade <= 0) {
    erros.quantidade = 'Informe uma quantidade inteira maior que zero.'
  } else if (dados.tipo === 'SAIDA' && dados.quantidade > saldo) {
    erros.quantidade = `A saída não pode superar o saldo disponível (${saldo}).`
  }
  if (!dados.motivo) erros.motivo = 'Informe o motivo da movimentação.'
  return erros
}
