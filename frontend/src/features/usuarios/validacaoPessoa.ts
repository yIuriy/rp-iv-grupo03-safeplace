import type { DadosPessoa } from './api'
import { hojeIso } from './formatos'

export type CampoPessoa = keyof DadosPessoa
export type ErrosPessoa = Partial<Record<CampoPessoa, string>>

export const ordemDosCampos: CampoPessoa[] = ['nome', 'cpf', 'dataNascimento', 'email']
const formatoEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function lerDadosPessoa(form: FormData): DadosPessoa {
  const texto = (campo: CampoPessoa) => String(form.get(campo) ?? '').trim()
  const email = texto('email')
  return { nome: texto('nome'), cpf: texto('cpf'), dataNascimento: texto('dataNascimento'), email: email || undefined }
}

/** Confere só o que o backend também exige: obrigatoriedade, 11 dígitos, data no passado e e-mail. */
export function validarPessoa(dados: DadosPessoa, emailObrigatorio: boolean): ErrosPessoa {
  const erros: ErrosPessoa = {}
  if (!dados.nome) erros.nome = 'Informe o nome completo.'
  if (dados.cpf.replace(/\D/g, '').length !== 11) erros.cpf = 'Informe um CPF com 11 dígitos.'
  if (!dados.dataNascimento) erros.dataNascimento = 'Informe a data de nascimento.'
  else if (dados.dataNascimento >= hojeIso()) erros.dataNascimento = 'A data de nascimento deve estar no passado.'
  if (emailObrigatorio && !dados.email) erros.email = 'Informe o e-mail corporativo usado no login.'
  else if (dados.email && !formatoEmail.test(dados.email)) erros.email = 'Informe um e-mail válido.'
  return erros
}
