// Leitura do FormData e validação local dos dados de pessoa, espelhando o que o backend exige:
// nome, CPF válido, data no passado e e-mail. Desde a issue #308, as regras são um esquema zod e os
// dígitos do CPF são conferidos pela biblioteca brazilian-utils, como o @CPF dos Requests.

import { isValidCPF } from '@brazilian-utils/brazilian-utils'
import { z } from 'zod'
import { errosPorCampo } from '../../shared/formularios/errosDoEsquema'
import type { DadosPessoa } from './api'
import { digitosDoCpf, hojeIso } from './formatos'

export type CampoPessoa = keyof DadosPessoa
export type ErrosPessoa = Partial<Record<CampoPessoa, string>>

export const ordemDosCampos: CampoPessoa[] = ['nome', 'cpf', 'dataNascimento', 'email']

export function lerDadosPessoa(form: FormData): DadosPessoa {
  const texto = (campo: CampoPessoa) => String(form.get(campo) ?? '').trim()
  const email = texto('email')
  return { nome: texto('nome'), cpf: digitosDoCpf(texto('cpf')), dataNascimento: texto('dataNascimento'), email: email || undefined }
}

function esquemaPessoa(emailObrigatorio: boolean, hoje: string) {
  const emailValido = z.email('Informe um e-mail válido.')
  return z.object({
    nome: z.string().min(1, 'Informe o nome completo.'),
    cpf: z.string()
      .refine(cpf => digitosDoCpf(cpf).length === 11, 'Informe um CPF com 11 dígitos.')
      .refine(isValidCPF, 'CPF inválido: confira os dígitos.'),
    dataNascimento: z.string()
      .min(1, 'Informe a data de nascimento.')
      .refine(data => data < hoje, 'A data de nascimento deve estar no passado.'),
    email: emailObrigatorio
      ? z.string('Informe o e-mail corporativo usado no login.').pipe(emailValido)
      : emailValido.optional(),
  })
}

/** Confere o que o backend também exige: obrigatoriedade, CPF válido, data no passado e e-mail. */
export function validarPessoa(dados: DadosPessoa, emailObrigatorio: boolean): ErrosPessoa {
  return errosPorCampo(esquemaPessoa(emailObrigatorio, hojeIso()).safeParse(dados))
}
