// Leitura e validação local do formulário de entrega (UC12, passos 3 a 5; US11): colaborador, EPI e
// data prevista de devolução obrigatórios, data não anterior a hoje e justificativa obrigatória na
// confirmação de duplicidade. As regras definitivas (saldo, CA, capacitação) são do backend; aqui só
// se evita uma chamada que já se sabe inválida.

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

/** `hoje` e a data prevista estão em `AAAA-MM-DD`, então a comparação de texto segue a ordem das datas. */
export function validarEntrega(rascunho: RascunhoEntrega, hoje: string): ErrosEntrega {
  const erros: ErrosEntrega = {}
  if (!rascunho.colaboradorId) erros.colaboradorId = 'Selecione o colaborador que recebe o EPI.'
  if (!rascunho.epiId) erros.epiId = 'Selecione o EPI que será entregue.'
  if (!rascunho.dataDevolucaoPrevista) {
    erros.dataDevolucaoPrevista = 'Informe a data prevista de devolução.'
  } else if (rascunho.dataDevolucaoPrevista < hoje) {
    erros.dataDevolucaoPrevista = 'A devolução prevista não pode ser anterior a hoje, dia da entrega.'
  }
  return erros
}

export function paraDadosDeEntrega(rascunho: RascunhoEntrega): DadosEntrega {
  return {
    epiId: Number(rascunho.epiId),
    colaboradorId: Number(rascunho.colaboradorId),
    dataDevolucaoPrevista: rascunho.dataDevolucaoPrevista,
  }
}

/** Dicionário da US11: justificativa condicional, obrigatória quando já existe item ativo do mesmo tipo. */
export function validarJustificativa(justificativa: string): string | undefined {
  return justificativa.trim() ? undefined : 'Informe a justificativa da substituição.'
}
