// Leitura e validação local das operações do plano de ação (UC07, contrato da issue #97): nova ação
// com ao menos uma medida, prazo não anterior a hoje nem à data do fato, exatamente um responsável e
// antecedência do alerta quando ativo; prorrogação com novo prazo posterior ao atual e justificativa.
// As regras definitivas, como a ação preventiva exigida em acidente grave, são do backend.

import type { DadosNovaAcao } from './apiPlanos'

export type CampoAcao = 'medidasCorretivas' | 'prazo' | 'responsavelTipo' | 'responsavelId' | 'antecedenciaAlertaDias'
export type ErrosAcao = Partial<Record<CampoAcao, string>>
export const ordemDosCamposDaAcao: readonly CampoAcao[] = ['medidasCorretivas', 'prazo', 'responsavelTipo', 'responsavelId', 'antecedenciaAlertaDias']

function texto(form: FormData, campo: string): string {
  return String(form.get(campo) ?? '').trim()
}

/** Antecedência em dias: inteiro maior ou igual a zero; `undefined` quando vazia ou inválida. */
function lerAntecedencia(valor: string): number | undefined {
  if (!valor) return undefined
  const numero = Number(valor)
  return Number.isInteger(numero) && numero >= 0 ? numero : Number.NaN
}

export type LeituraAcao = { dados: DadosNovaAcao; responsavelTipo: string }

export function lerNovaAcao(form: FormData): LeituraAcao {
  const responsavelTipo = texto(form, 'responsavelTipo')
  const responsavelId = Number(texto(form, 'responsavelId')) || undefined
  const alertaAtivo = form.get('alertaAtivo') === 'on'
  return {
    responsavelTipo,
    dados: {
      medidasCorretivas: texto(form, 'medidasCorretivas') || undefined,
      medidasPreventivas: texto(form, 'medidasPreventivas') || undefined,
      prazo: texto(form, 'prazo'),
      responsavelColaboradorId: responsavelTipo === 'COLABORADOR' ? responsavelId : undefined,
      responsavelSetorId: responsavelTipo === 'SETOR' ? responsavelId : undefined,
      alertaAtivo,
      antecedenciaAlertaDias: alertaAtivo ? lerAntecedencia(texto(form, 'antecedenciaAlertaDias')) : undefined,
    },
  }
}

/** `prazoMinimo` é o maior entre hoje e o dia do fato (AAAA-MM-DD). */
export function validarNovaAcao({ dados, responsavelTipo }: LeituraAcao, prazoMinimo: string): ErrosAcao {
  const erros: ErrosAcao = {}
  if (!dados.medidasCorretivas && !dados.medidasPreventivas) {
    erros.medidasCorretivas = 'Informe a medida corretiva, a preventiva ou ambas.'
  }
  if (!dados.prazo) erros.prazo = 'Informe o prazo da ação.'
  else if (dados.prazo < prazoMinimo) erros.prazo = 'O prazo não pode ser anterior a hoje nem à data do fato.'
  if (responsavelTipo !== 'COLABORADOR' && responsavelTipo !== 'SETOR') erros.responsavelTipo = 'Indique se o responsável é um colaborador ou um setor.'
  else if (!dados.responsavelColaboradorId && !dados.responsavelSetorId) {
    erros.responsavelId = responsavelTipo === 'SETOR' ? 'Selecione o setor responsável.' : 'Selecione o colaborador responsável.'
  }
  if (dados.alertaAtivo && (dados.antecedenciaAlertaDias === undefined || Number.isNaN(dados.antecedenciaAlertaDias))) {
    erros.antecedenciaAlertaDias = 'Com o alerta ativo, informe a antecedência em dias (número inteiro, zero ou mais).'
  }
  return erros
}

export type ErrosProrrogacao = Partial<Record<'novoPrazo' | 'justificativa', string>>

export function validarProrrogacao(novoPrazo: string, justificativa: string, prazoAtual: string, hoje: string): ErrosProrrogacao {
  const erros: ErrosProrrogacao = {}
  if (!novoPrazo) erros.novoPrazo = 'Informe o novo prazo.'
  else if (novoPrazo <= prazoAtual) erros.novoPrazo = 'O novo prazo deve ser posterior ao prazo atual.'
  else if (novoPrazo < hoje) erros.novoPrazo = 'O novo prazo não pode ser anterior a hoje.'
  if (!justificativa.trim()) erros.justificativa = 'A justificativa técnica é obrigatória para prorrogar.'
  return erros
}

export function validarAntecedencia(ativo: boolean, valor: string): string | undefined {
  if (!ativo) return undefined
  const numero = lerAntecedencia(valor.trim())
  return numero === undefined || Number.isNaN(numero) ? 'Informe a antecedência em dias (número inteiro, zero ou mais).' : undefined
}
