// Validação local do registro de inspeção (US09, issue #136), igual à da API: data obrigatória e não
// posterior a hoje, ao menos um item, e cada item com referência avaliada e resultado. Desde a issue
// #308, as regras são um esquema zod.

import { z } from 'zod'
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

const esquemaItem = z.object({
  descricao: z.string().trim().min(1, 'Informe a norma ou referência avaliada.'),
  resultado: z.enum(['conforme', 'nao-conforme'], 'Indique conformidade ou não conformidade.'),
})

function esquemaInspecao(hoje: string) {
  return z.object({
    dataInspecao: z.string()
      .min(1, 'Informe a data em que a inspeção foi realizada.')
      .refine(data => data <= hoje, 'A data da inspeção não pode ser posterior a hoje.'),
    itens: z.array(esquemaItem).min(1),
  })
}

/** Os erros de item ficam na chave do rascunho, para a linha certa mostrar a mensagem. */
function errosDaInspecao(problemas: z.core.$ZodIssue[], itens: RascunhoItem[]): ErrosInspecao {
  const erros: ErrosInspecao = { itens: {} }
  for (const { path, message } of problemas) {
    if (path[0] === 'dataInspecao') erros.dataInspecao ??= message
    if (path[0] !== 'itens' || typeof path[1] !== 'number') continue
    const campo = path[2] as keyof ErrosItem
    const chave = itens[path[1]].chave
    erros.itens[chave] = { ...erros.itens[chave], [campo]: erros.itens[chave]?.[campo] ?? message }
  }
  return erros
}

export function validarInspecao(dataInspecao: string, itens: RascunhoItem[], hoje: string): {
  dados?: DadosRegistroInspecao
  erros: ErrosInspecao
} {
  const resultado = esquemaInspecao(hoje).safeParse({ dataInspecao, itens })
  if (!resultado.success) return { erros: errosDaInspecao(resultado.error.issues, itens) }
  const erros: ErrosInspecao = { itens: {} }
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
