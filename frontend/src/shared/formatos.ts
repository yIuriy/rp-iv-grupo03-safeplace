/** Remove acentos e passa para minúsculas, para comparar termos de busca sem distinguir grafia. */
export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR')
}
