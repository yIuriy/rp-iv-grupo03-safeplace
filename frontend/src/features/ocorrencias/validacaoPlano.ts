// Leitura e validação local das operações do plano de ação (UC07, contrato da issue #97): nova ação
// com ao menos uma medida, prazo não anterior a hoje nem à data do fato, exatamente um responsável e
// antecedência do alerta quando ativo; prorrogação com novo prazo posterior ao atual e justificativa.
// As regras definitivas, como a ação preventiva exigida em acidente grave, são do backend. Desde a
// issue #308, as regras são esquemas zod.

import { z } from 'zod'
import { errosPorCampo, regraDoCampo } from '../../shared/formularios/errosDoEsquema'
import type { DadosNovaAcao } from './apiPlanos'

export type CampoAcao = 'medidasCorretivas' | 'prazo' | 'responsavelTipo' | 'responsavelId' | 'antecedenciaAlertaDias'
export type ErrosAcao = Partial<Record<CampoAcao, string>>
export const ordemDosCamposDaAcao: readonly CampoAcao[] = ['medidasCorretivas', 'prazo', 'responsavelTipo', 'responsavelId', 'antecedenciaAlertaDias']

function texto(form: FormData, campo: string): string {
  return String(form.get(campo) ?? '').trim()
}

/** Antecedência digitada em dias; `undefined` quando vazia. Quem confere o valor é o esquema. */
function lerAntecedencia(valor: string): number | undefined {
  return valor ? Number(valor) : undefined
}

/** Antecedência do alerta: número inteiro, zero ou mais. */
const antecedenciaValida = z.number().int().min(0)

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

function esquemaNovaAcao(prazoMinimo: string) {
  return z.object({
    medidasCorretivas: z.string().optional(),
    medidasPreventivas: z.string().optional(),
    prazo: z.string()
      .min(1, 'Informe o prazo da ação.')
      .refine(prazo => prazo >= prazoMinimo, 'O prazo não pode ser anterior a hoje nem à data do fato.'),
    responsavelTipo: z.enum(['COLABORADOR', 'SETOR'], 'Indique se o responsável é um colaborador ou um setor.'),
    responsavelColaboradorId: z.number().optional(),
    responsavelSetorId: z.number().optional(),
    alertaAtivo: z.boolean(),
    antecedenciaAlertaDias: z.unknown(),
  })
    .refine(a => Boolean(a.medidasCorretivas || a.medidasPreventivas),
      regraDoCampo('medidasCorretivas', 'Informe a medida corretiva, a preventiva ou ambas.'))
    .refine(a => a.responsavelTipo !== 'COLABORADOR' || Boolean(a.responsavelColaboradorId),
      regraDoCampo('responsavelId', 'Selecione o colaborador responsável.'))
    .refine(a => a.responsavelTipo !== 'SETOR' || Boolean(a.responsavelSetorId),
      regraDoCampo('responsavelId', 'Selecione o setor responsável.'))
    .refine(a => !a.alertaAtivo || antecedenciaValida.safeParse(a.antecedenciaAlertaDias).success,
      regraDoCampo('antecedenciaAlertaDias', 'Com o alerta ativo, informe a antecedência em dias (número inteiro, zero ou mais).'))
}

/** `prazoMinimo` é o maior entre hoje e o dia do fato (AAAA-MM-DD). */
export function validarNovaAcao({ dados, responsavelTipo }: LeituraAcao, prazoMinimo: string): ErrosAcao {
  return errosPorCampo(esquemaNovaAcao(prazoMinimo).safeParse({ ...dados, responsavelTipo }))
}

export type ErrosProrrogacao = Partial<Record<'novoPrazo' | 'justificativa', string>>

function esquemaProrrogacao(prazoAtual: string, hoje: string) {
  return z.object({
    novoPrazo: z.string()
      .min(1, 'Informe o novo prazo.')
      .refine(prazo => prazo > prazoAtual, 'O novo prazo deve ser posterior ao prazo atual.')
      .refine(prazo => prazo >= hoje, 'O novo prazo não pode ser anterior a hoje.'),
    justificativa: z.string().trim().min(1, 'A justificativa técnica é obrigatória para prorrogar.'),
  })
}

export function validarProrrogacao(novoPrazo: string, justificativa: string, prazoAtual: string, hoje: string): ErrosProrrogacao {
  return errosPorCampo(esquemaProrrogacao(prazoAtual, hoje).safeParse({ novoPrazo, justificativa }))
}

export function validarAntecedencia(ativo: boolean, valor: string): string | undefined {
  if (!ativo || antecedenciaValida.safeParse(lerAntecedencia(valor.trim())).success) return undefined
  return 'Informe a antecedência em dias (número inteiro, zero ou mais).'
}
