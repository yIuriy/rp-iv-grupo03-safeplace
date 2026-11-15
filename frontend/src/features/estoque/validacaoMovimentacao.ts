// Leitura e validação local do formulário de movimentação de estoque (US03): quantidade inteira
// maior que zero, saída limitada ao saldo conhecido e motivo obrigatório. A regra definitiva
// (saldo nunca negativo) é do backend; aqui só se evita uma chamada que já se sabe inválida. Desde a
// issue #308, as regras são um esquema zod.

import { z } from 'zod'
import { errosPorCampo } from '../../shared/formularios/errosDoEsquema'
import type { DadosMovimentacao } from './api'

export type ErrosMovimentacao = Partial<Record<'quantidade' | 'motivo', string>>

export const ordemDosCamposDeMovimentacao = ['quantidade', 'motivo'] as const

export function lerDadosMovimentacao(form: FormData): DadosMovimentacao {
  const tipo = form.get('tipo') === 'SAIDA' ? 'SAIDA' : 'ENTRADA'
  const quantidade = Number(String(form.get('quantidade') ?? '').trim())
  return { tipo, quantidade, motivo: String(form.get('motivo') ?? '').trim() }
}

const QUANTIDADE_INVALIDA = 'Informe uma quantidade inteira maior que zero.'

/** Na saída, a quantidade também fica limitada ao saldo conhecido. */
function esquemaMovimentacao(tipo: DadosMovimentacao['tipo'], saldo: number) {
  const quantidade = z.number(QUANTIDADE_INVALIDA).int(QUANTIDADE_INVALIDA).positive(QUANTIDADE_INVALIDA)
  return z.object({
    quantidade: tipo === 'SAIDA' ? quantidade.max(saldo, `A saída não pode superar o saldo disponível (${saldo}).`) : quantidade,
    motivo: z.string().min(1, 'Informe o motivo da movimentação.'),
  })
}

export function validarMovimentacao(dados: DadosMovimentacao, saldo: number): ErrosMovimentacao {
  return errosPorCampo(esquemaMovimentacao(dados.tipo, saldo).safeParse(dados))
}
