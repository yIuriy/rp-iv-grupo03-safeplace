// Validação local dos cadastros de capacitação (RF10, UC02, issue #192), igual à da API: campos
// obrigatórios, datas coerentes e validade de treinamento de ao menos 1 mês.

import type { DadosCertificacao, DadosExigencia, DadosTreinamento, AlvoExigencia, TipoCapacitacao } from './api'

export type Erros<T extends string> = Partial<Record<T, string>>

export type RascunhoCertificacao = { colaboradorId: string; nome: string; dataEmissao: string; dataVencimento: string }
export type CampoCertificacao = keyof RascunhoCertificacao

export type RascunhoTreinamento = {
  nome: string
  conteudo: string
  instrutor: string
  dataRealizacao: string
  cargaHoraria: string
  validadeMeses: string
  colaboradoresIds: number[]
}
export type CampoTreinamento = keyof RascunhoTreinamento

export type RascunhoExigencia = { nomeCapacitacao: string; tipoCapacitacao: TipoCapacitacao | ''; alvo: AlvoExigencia | ''; alvoId: string }
export type CampoExigencia = keyof RascunhoExigencia

type Resultado<D, C extends string> = { dados?: D; erros: Erros<C> }

function inteiroPositivo(valor: string): number | null {
  const numero = Number(valor)
  return valor.trim() !== '' && Number.isInteger(numero) && numero > 0 ? numero : null
}

export function validarCertificacao(r: RascunhoCertificacao, hoje: string): Resultado<DadosCertificacao, CampoCertificacao> {
  const erros: Erros<CampoCertificacao> = {}
  const colaboradorId = inteiroPositivo(r.colaboradorId)
  if (colaboradorId === null) erros.colaboradorId = 'Selecione o colaborador titular.'
  if (!r.nome.trim()) erros.nome = 'Informe o nome da certificação.'
  if (!r.dataEmissao) erros.dataEmissao = 'Informe a data de emissão.'
  if (!r.dataVencimento) erros.dataVencimento = 'Informe a data de vencimento.'
  else if (r.dataVencimento <= hoje) erros.dataVencimento = 'A data de vencimento deve ser posterior a hoje.'
  if (r.dataEmissao && r.dataVencimento && !erros.dataVencimento && r.dataEmissao >= r.dataVencimento) {
    erros.dataEmissao = 'A emissão deve ser anterior ao vencimento.'
  }
  if (Object.keys(erros).length > 0) return { erros }
  return { erros, dados: { nome: r.nome.trim(), dataEmissao: r.dataEmissao, dataVencimento: r.dataVencimento, colaboradorId: colaboradorId! } }
}

export function validarTreinamento(r: RascunhoTreinamento, hoje: string): Resultado<DadosTreinamento, CampoTreinamento> {
  const erros: Erros<CampoTreinamento> = {}
  if (!r.nome.trim()) erros.nome = 'Informe o nome do treinamento.'
  if (!r.dataRealizacao) erros.dataRealizacao = 'Informe a data de realização.'
  else if (r.dataRealizacao > hoje) erros.dataRealizacao = 'A data de realização não pode ser posterior a hoje.'
  if (r.cargaHoraria.trim() && inteiroPositivo(r.cargaHoraria) === null) erros.cargaHoraria = 'A carga horária deve ser um número inteiro positivo.'
  if (r.validadeMeses.trim() && inteiroPositivo(r.validadeMeses) === null) erros.validadeMeses = 'A validade deve ser de ao menos 1 mês.'
  if (r.colaboradoresIds.length === 0) erros.colaboradoresIds = 'Selecione ao menos um colaborador.'
  if (Object.keys(erros).length > 0) return { erros }
  const cargaHoraria = inteiroPositivo(r.cargaHoraria)
  const validadeMeses = inteiroPositivo(r.validadeMeses)
  return {
    erros,
    dados: {
      nome: r.nome.trim(),
      ...(r.conteudo.trim() ? { conteudo: r.conteudo.trim() } : {}),
      ...(r.instrutor.trim() ? { instrutor: r.instrutor.trim() } : {}),
      dataRealizacao: r.dataRealizacao,
      ...(cargaHoraria !== null ? { cargaHoraria } : {}),
      ...(validadeMeses !== null ? { validadeMeses } : {}),
      colaboradoresIds: r.colaboradoresIds,
    },
  }
}

export function validarExigencia(r: RascunhoExigencia): Resultado<DadosExigencia, CampoExigencia> {
  const erros: Erros<CampoExigencia> = {}
  if (!r.nomeCapacitacao.trim()) erros.nomeCapacitacao = 'Informe a certificação ou o treinamento exigido.'
  if (!r.tipoCapacitacao) erros.tipoCapacitacao = 'Indique se a exigência é de certificação ou de treinamento.'
  if (!r.alvo) erros.alvo = 'Indique se a exigência vale para uma tarefa ou para um EPI.'
  const alvoId = inteiroPositivo(r.alvoId)
  if (r.alvo && alvoId === null) erros.alvoId = r.alvo === 'TAREFA' ? 'Selecione a tarefa.' : 'Selecione o EPI.'
  if (Object.keys(erros).length > 0) return { erros }
  return {
    erros,
    dados: { nomeCapacitacao: r.nomeCapacitacao.trim(), tipoCapacitacao: r.tipoCapacitacao as TipoCapacitacao, alvo: r.alvo as AlvoExigencia, alvoId: alvoId! },
  }
}
