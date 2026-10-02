// Leitura do FormData e validação local do cadastro de setor (UC03, issue #174), espelhando o que
// o backend exige: nome e grau obrigatórios (`CriarSetorRequest`) e os tamanhos das colunas da V13.

import { ehNivelPerigo, type DadosCadastroSetor } from './api'

export type CampoSetor = 'nome' | 'status' | 'localizacao'
export type ErrosSetor = Partial<Record<CampoSetor, string>>

/** Ordem visual dos campos validados, usada para focar o primeiro erro. */
export const ordemDosCamposSetor: CampoSetor[] = ['nome', 'status', 'localizacao']

/** Limites de `setores.nome` (VARCHAR 160) e `setores.localizacao` (VARCHAR 255) na migração V13. */
export const TAMANHO_MAXIMO_NOME = 160
export const TAMANHO_MAXIMO_LOCALIZACAO = 255

type Leitura = { dados: DadosCadastroSetor | null; erros: ErrosSetor }

/** Lê o formulário e devolve o corpo pronto para a API, ou os erros a mostrar sem enviar nada. */
export function lerDadosSetor(form: FormData): Leitura {
  const texto = (campo: string) => String(form.get(campo) ?? '').trim()
  const nome = texto('nome')
  const status = texto('status')
  const localizacao = texto('localizacao')
  const descricao = texto('descricao')

  const erros: ErrosSetor = {}
  if (!nome) erros.nome = 'Informe o nome do setor.'
  else if (nome.length > TAMANHO_MAXIMO_NOME) erros.nome = `Use no máximo ${TAMANHO_MAXIMO_NOME} caracteres.`
  if (!ehNivelPerigo(status)) erros.status = 'Selecione o grau de perigo do setor.'
  if (localizacao.length > TAMANHO_MAXIMO_LOCALIZACAO) {
    erros.localizacao = `Use no máximo ${TAMANHO_MAXIMO_LOCALIZACAO} caracteres.`
  }

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
