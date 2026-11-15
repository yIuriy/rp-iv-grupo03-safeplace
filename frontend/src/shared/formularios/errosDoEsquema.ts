// Ponte entre os esquemas zod (issue #308) e os formulários: transforma o resultado do safeParse no
// objeto de erros que o Template Method de envio (#233) já usa, com uma mensagem por campo.

import type { z } from 'zod'

type Resultado = ReturnType<z.ZodType['safeParse']>

/** Primeira mensagem de cada campo, na ordem em que o esquema conferiu as regras. */
export function errosPorCampo<C extends string>(resultado: Resultado): Partial<Record<C, string>> {
  const erros: Partial<Record<C, string>> = {}
  if (resultado.success) return erros
  for (const problema of resultado.error.issues) {
    const campo = String(problema.path[0]) as C
    erros[campo] ??= problema.message
  }
  return erros
}

/**
 * Opções de uma regra que compara campos (`.refine` no objeto): a mensagem vai para `campo` e a regra
 * roda mesmo quando outro campo já falhou, para o formulário mostrar todas as pendências de uma vez.
 */
export function regraDoCampo(campo: string, message: string) {
  return { message, path: [campo], when: () => true }
}
