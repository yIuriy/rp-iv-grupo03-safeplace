// Rótulos em português para os valores de ocorrência vindos da API (tipo, causa raiz e gravidade),
// usados na lista e no detalhe. Valor desconhecido aparece como veio, sem ser descartado.

import { ehNivelPerigo, rotuloNivelPerigo } from '../areas-risco/api'
import type { TipoOcorrencia } from './api'

export const rotuloTipoOcorrencia: Record<TipoOcorrencia, string> = {
  ACIDENTE: 'Acidente',
  INCIDENTE: 'Incidente',
}

const rotuloCausaRaiz: Record<string, string> = {
  FATOR_HUMANO: 'Fator humano',
  FALHA_EPI: 'Falha de EPI',
  OUTRO: 'Outra causa',
}

export function descreverCausaRaiz(valor: string | null | undefined): string {
  if (!valor) return 'Não informada'
  return rotuloCausaRaiz[valor] ?? valor
}

/** A gravidade do acidente usa o vocabulário NivelPerigo (decisão DS09). */
export function descreverGravidade(valor: string | null | undefined): string {
  if (!valor) return 'Não informada'
  return ehNivelPerigo(valor) ? rotuloNivelPerigo[valor] : valor
}

export function textoOuPadrao(valor: string | null | undefined, padrao = 'Não informado'): string {
  return valor && valor.trim() ? valor : padrao
}
