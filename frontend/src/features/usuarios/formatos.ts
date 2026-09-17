export function formatarCpf(cpf: string): string {
  const digitos = cpf.replace(/\D/g, '')
  return digitos.length === 11 ? digitos.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4') : cpf
}

/** Converte `AAAA-MM-DD` da API para `DD/MM/AAAA`; devolve o original se o formato for outro. */
export function formatarData(iso: string): string {
  const [ano, mes, dia] = iso.split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso
}

/** Data local de hoje em `AAAA-MM-DD`, comparável com o valor de um `<input type="date">`. */
export function hojeIso(): string {
  const agora = new Date()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  const dia = String(agora.getDate()).padStart(2, '0')
  return `${agora.getFullYear()}-${mes}-${dia}`
}
