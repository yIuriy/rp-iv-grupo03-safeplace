// Primitivas de leitura das respostas da API (padrão Adapter, issue #244). As funções `lerX` de cada
// `api.ts` são os adaptadores entre o JSON devolvido pelo axios (`unknown`) e os tipos das features;
// elas se apoiam nestas primitivas para que o mesmo campo seja lido do mesmo jeito em todas as telas.
// Antes, cada arquivo tinha a sua cópia, e a de `estoque` aceitava texto em branco.

/** Objeto JSON já conferido, com os campos ainda sem tipo. */
export type Bruto = Record<string, unknown>

/**
 * Texto com conteúdo, devolvido exatamente como veio (sem `trim`). Texto vazio ou só com espaços,
 * ausência e qualquer outro tipo viram `null`, para a tela mostrar a sua reserva ("Não informado").
 */
export function textoOuNulo(valor: unknown): string | null {
  return typeof valor === 'string' && valor.trim() ? valor : null
}

/** Como {@link textoOuNulo}, mas com a reserva da tela no lugar de `null`. */
export function texto(valor: unknown, reserva: string): string {
  return textoOuNulo(valor) ?? reserva
}

/** Número como veio; ausência e qualquer outro tipo (inclusive texto numérico) viram `null`. */
export function numeroOuNulo(valor: unknown): number | null {
  return typeof valor === 'number' ? valor : null
}

/**
 * Objeto JSON (nem `null`, nem lista, nem valor simples). Fora disso, lança `Error(mensagem)`: a
 * resposta não segue o contrato da API e a tela mostra a falha de leitura.
 */
export function objeto(dado: unknown, mensagem: string): Bruto {
  if (dado === null || typeof dado !== 'object' || Array.isArray(dado)) throw new Error(mensagem)
  return dado as Bruto
}

/** Lista obrigatória: cada item passa por `ler`. Se não for lista, lança `Error(mensagem)`. */
export function lista<T>(dado: unknown, ler: (item: unknown) => T, mensagem: string): T[] {
  if (!Array.isArray(dado)) throw new Error(mensagem)
  return dado.map(ler)
}

/** Lista opcional: cada item passa por `ler`; ausência ou outro tipo viram lista vazia. */
export function listaOuVazia<T>(dado: unknown, ler: (item: unknown) => T): T[] {
  return Array.isArray(dado) ? dado.map(ler) : []
}
