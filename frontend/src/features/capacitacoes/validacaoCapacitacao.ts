// Validação local dos cadastros de capacitação (RF10, UC02, issue #192), igual à da API: campos
// obrigatórios, datas coerentes e validade de treinamento de ao menos 1 mês. Desde a issue #308, as
// regras são esquemas zod, que também convertem os números digitados.

import { z } from 'zod'
import { errosPorCampo, regraDoCampo } from '../../shared/formularios/errosDoEsquema'
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

/** Texto de um campo numérico convertido para inteiro maior que zero. */
const inteiroPositivo = (mensagem: string) => z.coerce.number(mensagem).int(mensagem).positive(mensagem)

/** Campo numérico opcional: vazio fica de fora, preenchido precisa ser inteiro maior que zero. */
const inteiroPositivoOpcional = (mensagem: string) =>
  z.preprocess(valor => (String(valor).trim() === '' ? undefined : valor), inteiroPositivo(mensagem).optional())

const textoObrigatorio = (mensagem: string) => z.string().trim().min(1, mensagem)

/** Texto opcional: espaços são removidos e o vazio fica de fora do corpo enviado. */
const textoOpcional = z.string().trim().transform(valor => valor || undefined)

function esquemaCertificacao(hoje: string) {
  return z.object({
    colaboradorId: inteiroPositivo('Selecione o colaborador titular.'),
    nome: textoObrigatorio('Informe o nome da certificação.'),
    dataEmissao: z.string().min(1, 'Informe a data de emissão.'),
    dataVencimento: z.string()
      .min(1, 'Informe a data de vencimento.')
      .refine(data => data > hoje, 'A data de vencimento deve ser posterior a hoje.'),
  }).refine(
    r => !r.dataEmissao || !(r.dataVencimento > hoje) || r.dataEmissao < r.dataVencimento,
    regraDoCampo('dataEmissao', 'A emissão deve ser anterior ao vencimento.'),
  )
}

export function validarCertificacao(r: RascunhoCertificacao, hoje: string): Resultado<DadosCertificacao, CampoCertificacao> {
  const resultado = esquemaCertificacao(hoje).safeParse(r)
  return resultado.success ? { erros: {}, dados: resultado.data } : { erros: errosPorCampo(resultado) }
}

function esquemaTreinamento(hoje: string) {
  return z.object({
    nome: textoObrigatorio('Informe o nome do treinamento.'),
    conteudo: textoOpcional,
    instrutor: textoOpcional,
    dataRealizacao: z.string()
      .min(1, 'Informe a data de realização.')
      .refine(data => data <= hoje, 'A data de realização não pode ser posterior a hoje.'),
    cargaHoraria: inteiroPositivoOpcional('A carga horária deve ser um número inteiro positivo.'),
    validadeMeses: inteiroPositivoOpcional('A validade deve ser de ao menos 1 mês.'),
    colaboradoresIds: z.array(z.number()).min(1, 'Selecione ao menos um colaborador.'),
  })
}

/** Campos opcionais sem valor não vão no corpo enviado à API. */
function semVazios<T extends object>(dados: T): T {
  return Object.fromEntries(Object.entries(dados).filter(([, valor]) => valor !== undefined)) as T
}

export function validarTreinamento(r: RascunhoTreinamento, hoje: string): Resultado<DadosTreinamento, CampoTreinamento> {
  const resultado = esquemaTreinamento(hoje).safeParse(r)
  return resultado.success ? { erros: {}, dados: semVazios(resultado.data) } : { erros: errosPorCampo(resultado) }
}

const idSelecionado = inteiroPositivo('Selecione o item.')

const esquemaExigencia = z.object({
  nomeCapacitacao: textoObrigatorio('Informe a certificação ou o treinamento exigido.'),
  tipoCapacitacao: z.enum(['CERTIFICACAO', 'TREINAMENTO'], 'Indique se a exigência é de certificação ou de treinamento.'),
  alvo: z.enum(['TAREFA', 'EPI'], 'Indique se a exigência vale para uma tarefa ou para um EPI.'),
  alvoId: z.string(),
})
  .refine(r => r.alvo !== 'TAREFA' || idSelecionado.safeParse(r.alvoId).success, regraDoCampo('alvoId', 'Selecione a tarefa.'))
  .refine(r => r.alvo !== 'EPI' || idSelecionado.safeParse(r.alvoId).success, regraDoCampo('alvoId', 'Selecione o EPI.'))

export function validarExigencia(r: RascunhoExigencia): Resultado<DadosExigencia, CampoExigencia> {
  const resultado = esquemaExigencia.safeParse(r)
  if (!resultado.success) return { erros: errosPorCampo(resultado) }
  return { erros: {}, dados: { ...resultado.data, alvoId: idSelecionado.parse(resultado.data.alvoId) } }
}
