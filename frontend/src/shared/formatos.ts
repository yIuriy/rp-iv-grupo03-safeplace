/** Remove acentos e passa para minúsculas, para comparar termos de busca sem distinguir grafia. */
export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR')
}

// As funções de data cortam o texto ISO em vez de passar por `Date`, que deslocaria o dia e a hora
// pelo fuso do navegador.

/** `AAAA-MM-DD` (ou o início de um `AAAA-MM-DDTHH:mm`) para `DD/MM/AAAA`; devolve o original se o formato for outro. */
export function formatarData(iso: string): string {
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso
}

/** `AAAA-MM-DDTHH:mm[:ss]` para `DD/MM/AAAA HH:mm`; `conector` troca o espaço entre data e hora, como `' às '`. */
export function formatarDataHora(iso: string, conector = ' '): string {
  const [data, hora] = iso.split('T')
  return hora ? `${formatarData(data)}${conector}${hora.slice(0, 5)}` : formatarData(iso)
}
