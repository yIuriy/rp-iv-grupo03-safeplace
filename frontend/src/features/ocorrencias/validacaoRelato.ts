// Leitura e validação local do relato de ocorrência (UC09, exceção II; UC05): tipo, data do fato não
// futura, setor e área de risco, colaborador envolvido e descrição, mais os campos próprios de
// acidente (causa e gravidade) e de incidente (situação de risco e potencial de dano), exigidos pelo
// domínio. A regra definitiva é do backend; aqui se evita um envio que já se sabe inválido. Desde a
// issue #308, as regras são um esquema zod.

import { z } from 'zod'
import { errosPorCampo, regraDoCampo } from '../../shared/formularios/errosDoEsquema'
import type { DadosOcorrencia, TipoOcorrencia } from './api'

export type CampoRelato = 'tipoOcorrencia' | 'dataOcorrencia' | 'setorId' | 'areaId' | 'colaboradorId' | 'descricao'
  | 'causaRaiz' | 'gravidade' | 'situacaoRisco' | 'potencialDano'

export type ErrosRelato = Partial<Record<CampoRelato, string>>

export const ordemDosCamposDoRelato: readonly CampoRelato[] = [
  'tipoOcorrencia', 'dataOcorrencia', 'setorId', 'areaId', 'colaboradorId', 'descricao',
  'causaRaiz', 'gravidade', 'situacaoRisco', 'potencialDano',
]

export type LeituraRelato = {
  tipo: TipoOcorrencia | ''
  setorId: string
  dados: DadosOcorrencia
}

function texto(form: FormData, campo: string): string {
  return String(form.get(campo) ?? '').trim()
}

function opcional(valor: string): string | undefined {
  return valor || undefined
}

/** `AAAA-MM-DDTHH:MM` do campo vira `AAAA-MM-DDTHH:MM:00`, o LocalDateTime esperado pela API. */
export function paraDataHoraApi(valor: string): string {
  return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(valor) ? `${valor}:00` : valor
}

/** Data e hora locais de agora no formato do `<input type="datetime-local">`. */
export function agoraLocal(): string {
  const agora = new Date()
  const doisDigitos = (n: number) => String(n).padStart(2, '0')
  return `${agora.getFullYear()}-${doisDigitos(agora.getMonth() + 1)}-${doisDigitos(agora.getDate())}`
    + `T${doisDigitos(agora.getHours())}:${doisDigitos(agora.getMinutes())}`
}

export function lerRelato(form: FormData, tipoFixo?: TipoOcorrencia): LeituraRelato {
  const tipoLido = tipoFixo ?? texto(form, 'tipoOcorrencia')
  const tipo = tipoLido === 'ACIDENTE' || tipoLido === 'INCIDENTE' ? tipoLido : ''
  const dados: DadosOcorrencia = {
    dataOcorrencia: paraDataHoraApi(texto(form, 'dataOcorrencia')),
    descricao: texto(form, 'descricao'),
    areaId: Number(texto(form, 'areaId')) || 0,
    colaboradorId: Number(texto(form, 'colaboradorId')) || 0,
  }
  if (tipo === 'ACIDENTE') {
    dados.causaRaiz = opcional(texto(form, 'causaRaiz'))
    dados.gravidade = opcional(texto(form, 'gravidade'))
    dados.tipo = opcional(texto(form, 'tipo'))
    dados.dano = opcional(texto(form, 'dano'))
    dados.destino = opcional(texto(form, 'destino'))
  }
  if (tipo === 'INCIDENTE') {
    dados.situacaoRisco = opcional(texto(form, 'situacaoRisco'))
    dados.potencialDano = opcional(texto(form, 'potencialDano'))
  }
  return { tipo, setorId: texto(form, 'setorId'), dados }
}

/** Campo exigido só para um dos tipos: acidente pede causa e gravidade, incidente pede risco e dano. */
function exigidoNoTipo(tipo: TipoOcorrencia, campo: CampoRelato, mensagem: string) {
  return [
    (relato: Record<string, unknown>) => relato.tipoOcorrencia !== tipo || Boolean(relato[campo]),
    regraDoCampo(campo, mensagem),
  ] as const
}

function esquemaRelato(agora: string) {
  return z.object({
    tipoOcorrencia: z.enum(['ACIDENTE', 'INCIDENTE'], 'Selecione se foi um acidente ou um incidente.'),
    dataOcorrencia: z.string()
      .min(1, 'Informe a data e a hora do fato.')
      .refine(data => data.slice(0, 16) <= agora, 'A data do fato não pode estar no futuro.'),
    setorId: z.string().min(1, 'Selecione o setor onde o fato ocorreu.'),
    areaId: z.number(),
    colaboradorId: z.number().positive('Selecione o colaborador envolvido.'),
    descricao: z.string().min(1, 'Descreva o que aconteceu.'),
  }).loose()
    .refine(r => Boolean(r.areaId || r.setorId), regraDoCampo('areaId', 'Selecione o setor e depois a área de risco.'))
    .refine(r => Boolean(r.areaId) || !r.setorId, regraDoCampo('areaId', 'Selecione a área de risco do setor.'))
    .refine(...exigidoNoTipo('ACIDENTE', 'causaRaiz', 'Indique a causa: fator humano, falha de EPI ou outra.'))
    .refine(...exigidoNoTipo('ACIDENTE', 'gravidade', 'Indique a gravidade do acidente.'))
    .refine(...exigidoNoTipo('INCIDENTE', 'situacaoRisco', 'Descreva a situação de risco observada.'))
    .refine(...exigidoNoTipo('INCIDENTE', 'potencialDano', 'Informe o potencial de dano.'))
}

export function validarRelato({ tipo, setorId, dados }: LeituraRelato, agora: string = agoraLocal()): ErrosRelato {
  return errosPorCampo(esquemaRelato(agora).safeParse({ ...dados, tipoOcorrencia: tipo, setorId }))
}
