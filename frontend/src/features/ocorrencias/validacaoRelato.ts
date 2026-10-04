// Leitura e validação local do relato de ocorrência (UC09, exceção II; UC05): tipo, data do fato não
// futura, setor e área de risco, colaborador envolvido e descrição, mais os campos próprios de
// acidente (causa e gravidade) e de incidente (situação de risco e potencial de dano), exigidos pelo
// domínio. A regra definitiva é do backend; aqui se evita um envio que já se sabe inválido.

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

export function validarRelato({ tipo, setorId, dados }: LeituraRelato, agora: string = agoraLocal()): ErrosRelato {
  const erros: ErrosRelato = {}
  if (!tipo) erros.tipoOcorrencia = 'Selecione se foi um acidente ou um incidente.'
  if (!dados.dataOcorrencia) erros.dataOcorrencia = 'Informe a data e a hora do fato.'
  else if (dados.dataOcorrencia.slice(0, 16) > agora) erros.dataOcorrencia = 'A data do fato não pode estar no futuro.'
  if (!setorId) erros.setorId = 'Selecione o setor onde o fato ocorreu.'
  if (!dados.areaId) erros.areaId = setorId ? 'Selecione a área de risco do setor.' : 'Selecione o setor e depois a área de risco.'
  if (!dados.colaboradorId) erros.colaboradorId = 'Selecione o colaborador envolvido.'
  if (!dados.descricao) erros.descricao = 'Descreva o que aconteceu.'
  if (tipo === 'ACIDENTE') {
    if (!dados.causaRaiz) erros.causaRaiz = 'Indique a causa: fator humano, falha de EPI ou outra.'
    if (!dados.gravidade) erros.gravidade = 'Indique a gravidade do acidente.'
  }
  if (tipo === 'INCIDENTE') {
    if (!dados.situacaoRisco) erros.situacaoRisco = 'Descreva a situação de risco observada.'
    if (!dados.potencialDano) erros.potencialDano = 'Informe o potencial de dano.'
  }
  return erros
}
