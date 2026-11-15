// Leitura do FormData e validação local do cadastro de setor (UC03, issue #174), espelhando o que
// o backend exige: nome e grau obrigatórios (`CriarSetorRequest`) e os tamanhos das colunas da V13.
// Desde a issue #308, as regras são um esquema zod.

import { z } from 'zod'
import { errosPorCampo } from '../../shared/formularios/errosDoEsquema'
import { ehNivelPerigo, type DadosCadastroSetor } from './api'

export type CampoSetor = 'nome' | 'status' | 'localizacao'
export type ErrosSetor = Partial<Record<CampoSetor, string>>

/** Ordem visual dos campos validados, usada para focar o primeiro erro. */
export const ordemDosCamposSetor: CampoSetor[] = ['nome', 'status', 'localizacao']

/** Limites de `setores.nome` (VARCHAR 160) e `setores.localizacao` (VARCHAR 255) na migração V13. */
export const TAMANHO_MAXIMO_NOME = 160
export const TAMANHO_MAXIMO_LOCALIZACAO = 255

type Leitura = { dados: DadosCadastroSetor | null; erros: ErrosSetor }

const limite = (maximo: number) => `Use no máximo ${maximo} caracteres.`

const esquemaSetor = z.object({
  nome: z.string().min(1, 'Informe o nome do setor.').max(TAMANHO_MAXIMO_NOME, limite(TAMANHO_MAXIMO_NOME)),
  status: z.string().refine(ehNivelPerigo, 'Selecione o grau de perigo do setor.'),
  localizacao: z.string().max(TAMANHO_MAXIMO_LOCALIZACAO, limite(TAMANHO_MAXIMO_LOCALIZACAO)),
})

/** Lê o formulário e devolve o corpo pronto para a API, ou os erros a mostrar sem enviar nada. */
export function lerDadosSetor(form: FormData): Leitura {
  const texto = (campo: string) => String(form.get(campo) ?? '').trim()
  const nome = texto('nome')
  const status = texto('status')
  const localizacao = texto('localizacao')
  const descricao = texto('descricao')

  const erros = errosPorCampo<CampoSetor>(esquemaSetor.safeParse({ nome, status, localizacao }))

  if (Object.keys(erros).length > 0 || !ehNivelPerigo(status)) return { dados: null, erros }
  // Campos opcionais vazios não são enviados: o backend os guarda como nulos.
  return {
    dados: {
      nome,
      status,
      ...(localizacao && { localizacao }),
      ...(descricao && { descricao }),
    },
    erros,
  }
}
