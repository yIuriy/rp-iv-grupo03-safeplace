// Período dos relatórios (RF18, issue #299): a tela oferece recorte mensal ou anual e envia à API o
// primeiro e o último dia. O backend valida de novo (PeriodoRelatorioValidador).

export type Recorte = 'mensal' | 'anual'

/** Datas em `AAAA-MM-DD`, como a API espera. */
export type Periodo = { inicio: string; fim: string }

function doisDigitos(valor: number): string {
  return String(valor).padStart(2, '0')
}

/** Mês corrente em `AAAA-MM`, valor do campo `type="month"`. */
export function mesAtual(hoje = new Date()): string {
  return `${hoje.getFullYear()}-${doisDigitos(hoje.getMonth() + 1)}`
}

export function anoAtual(hoje = new Date()): number {
  return hoje.getFullYear()
}

/** `AAAA-MM` vira do dia 1 ao último dia do mês; `null` quando o valor não é um mês válido. */
export function periodoDoMes(mes: string): Periodo | null {
  const partes = /^(\d{4})-(\d{2})$/.exec(mes)
  if (!partes) return null
  const ano = Number(partes[1])
  const numeroMes = Number(partes[2])
  if (numeroMes < 1 || numeroMes > 12) return null
  const ultimoDia = new Date(ano, numeroMes, 0).getDate()
  return { inicio: `${partes[1]}-${partes[2]}-01`, fim: `${partes[1]}-${partes[2]}-${doisDigitos(ultimoDia)}` }
}

/** Ano inteiro, de 1º de janeiro a 31 de dezembro; `null` fora de 2000 a 2100. */
export function periodoDoAno(ano: number): Periodo | null {
  if (!Number.isInteger(ano) || ano < 2000 || ano > 2100) return null
  return { inicio: `${ano}-01-01`, fim: `${ano}-12-31` }
}
