// Leitura e validação local do formulário de entrega (UC12, passos 3 a 5; US11): colaborador, EPI e
// data prevista de devolução obrigatórios, data não anterior a hoje e justificativa obrigatória na
// confirmação de duplicidade. As regras definitivas (saldo, CA, capacitação) são do backend; aqui só
// se evita uma chamada que já se sabe inválida. Desde a issue #308, as regras são esquemas zod.

import { z } from 'zod'
import { errosPorCampo } from '../../shared/formularios/errosDoEsquema'
import type { DadosEntrega } from './api'

export type CampoEntrega = 'colaboradorId' | 'epiId' | 'dataDevolucaoPrevista'
export type ErrosEntrega = Partial<Record<CampoEntrega, string>>

/** Valores digitados, guardados como texto para repor o formulário ao voltar da duplicidade. */
export type RascunhoEntrega = Record<CampoEntrega, string>

export const rascunhoVazio: RascunhoEntrega = { colaboradorId: '', epiId: '', dataDevolucaoPrevista: '' }

export const ordemDosCamposDeEntrega: readonly CampoEntrega[] = ['colaboradorId', 'epiId', 'dataDevolucaoPrevista']

export function lerRascunho(form: FormData): RascunhoEntrega {
  const valor = (campo: CampoEntrega) => String(form.get(campo) ?? '').trim()
  return { colaboradorId: valor('colaboradorId'), epiId: valor('epiId'), dataDevolucaoPrevista: valor('dataDevolucaoPrevista') }
}

function esquemaEntrega(hoje: string) {
  return z.object({
    colaboradorId: z.string().min(1, 'Selecione o colaborador que recebe o EPI.'),
    epiId: z.string().min(1, 'Selecione o EPI que será entregue.'),
    dataDevolucaoPrevista: z.string()
      .min(1, 'Informe a data prevista de devolução.')
      .refine(data => data >= hoje, 'A devolução prevista não pode ser anterior a hoje, dia da entrega.'),
  })
}

/** `hoje` e a data prevista estão em `AAAA-MM-DD`, então a comparação de texto segue a ordem das datas. */
export function validarEntrega(rascunho: RascunhoEntrega, hoje: string): ErrosEntrega {
  return errosPorCampo(esquemaEntrega(hoje).safeParse(rascunho))
}

const justificativaObrigatoria = z.string().trim().min(1, 'Informe a justificativa da substituição.')

export function paraDadosDeEntrega(rascunho: RascunhoEntrega): DadosEntrega {
  return {
    epiId: Number(rascunho.epiId),
    colaboradorId: Number(rascunho.colaboradorId),
    dataDevolucaoPrevista: rascunho.dataDevolucaoPrevista,
  }
}

/** Dicionário da US11: justificativa condicional, obrigatória quando já existe item ativo do mesmo tipo. */
export function validarJustificativa(justificativa: string): string | undefined {
  return justificativaObrigatoria.safeParse(justificativa).error?.issues[0].message
}
