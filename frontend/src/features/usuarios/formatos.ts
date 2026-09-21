// Funções puras de formatação: dígitos, máscara e ocultação de CPF, e data ISO para DD/MM/AAAA.

/** Mantém só os dígitos e limita a 11, o tamanho de um CPF. */
export function digitosDoCpf(valor: string): string {
  return valor.replace(/\D/g, '').slice(0, 11)
}

/**
 * Máscara progressiva `000.000.000-00`: funciona para valores parciais, então serve enquanto o
 * usuário digita. Caracteres que não são dígitos são descartados.
 */
export function mascararCpf(valor: string): string {
  const digitos = digitosDoCpf(valor)
  const blocos = [digitos.slice(0, 3), digitos.slice(3, 6), digitos.slice(6, 9)].filter(Boolean).join('.')
  const verificador = digitos.slice(9)
  return verificador ? `${blocos}-${verificador}` : blocos
}

/**
 * Exibição para listagens: `•••.456.789-••`. Cobre com pontos o início e o fim, mantendo os seis
 * dígitos do meio para distinguir cadastros. Devolve o original se não tiver 11 dígitos.
 */
export function ocultarCpf(cpf: string): string {
  const digitos = digitosDoCpf(cpf)
  return digitos.length === 11 ? `•••.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-••` : cpf
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
