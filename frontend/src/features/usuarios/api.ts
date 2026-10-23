// Cliente tipado da API de usuários (/api/usuarios): tipos Usuario e DadosPessoa, validação das
// respostas e funções de listar, cadastrar e atualizar supervisores e colaboradores (RF23, issues
// #94 e #122), além da consulta do perfil da pessoa logada.

import { http } from '../../shared/api/https'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'

export type PerfilUsuario = 'SUPERVISOR' | 'GESTOR_SEGURANCA'

export type Usuario = {
  id: number
  nome: string
  cpf: string
  dataNascimento: string
  email: string | null
  perfil: PerfilUsuario | null
  ativo: boolean
}

/** Resposta de criação. `senhaInicial` só vem no cadastro de Supervisor e nunca em listagens. */
export type UsuarioCriado = Usuario & { senhaInicial?: string }

export type DadosPessoa = { nome: string; cpf: string; dataNascimento: string; email?: string }

export type FiltroColaboradores = { nome?: string; cpf?: string }

const perfis: readonly PerfilUsuario[] = ['SUPERVISOR', 'GESTOR_SEGURANCA']

function ehPerfil(valor: unknown): valor is PerfilUsuario {
  return typeof valor === 'string' && perfis.includes(valor as PerfilUsuario)
}

function lerUsuario(dado: unknown): UsuarioCriado {
  if (dado === null || typeof dado !== 'object') throw new Error('Registro de usuário inválido')
  const bruto = dado as Record<string, unknown>
  if (typeof bruto.id !== 'number' || typeof bruto.nome !== 'string' || typeof bruto.cpf !== 'string'
    || typeof bruto.dataNascimento !== 'string' || (bruto.perfil != null && !ehPerfil(bruto.perfil))) {
    throw new Error('Registro de usuário incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    nome: bruto.nome,
    cpf: bruto.cpf,
    dataNascimento: bruto.dataNascimento,
    email: typeof bruto.email === 'string' ? bruto.email : null,
    perfil: ehPerfil(bruto.perfil) ? bruto.perfil : null,
    ativo: bruto.ativo !== false,
    senhaInicial: typeof bruto.senhaInicial === 'string' ? bruto.senhaInicial : undefined,
  }
}

function lerLista(dados: unknown): Usuario[] {
  if (!Array.isArray(dados)) throw new Error('Esperava uma lista de usuários')
  return dados.map(lerUsuario)
}

/** `GET /api/usuarios` devolve contas e colaboradores; a tela do Gestor mostra só os supervisores. */
export async function listarSupervisores(signal?: AbortSignal): Promise<Usuario[]> {
  const { data } = await http.get<unknown>('/usuarios', { signal })
  return lerLista(data).filter(usuario => usuario.perfil === 'SUPERVISOR')
}

export async function listarColaboradores(filtro: FiltroColaboradores, signal?: AbortSignal): Promise<Usuario[]> {
  const params: Record<string, string> = {}
  if (filtro.nome) params.nome = filtro.nome
  if (filtro.cpf) params.cpf = filtro.cpf
  const { data } = await http.get<unknown>('/usuarios/colaboradores', { params, signal })
  return lerLista(data)
}

/** Dados da pessoa logada, com a data do cadastro (`criadoEm`, data e hora ISO, ou nulo). */
export type MeuPerfil = Usuario & { criadoEm: string | null }

/** `GET /api/usuarios/me`: o servidor identifica a pessoa pelo token, sem receber id. */
export async function buscarMeuPerfil(signal?: AbortSignal): Promise<MeuPerfil> {
  const { data } = await http.get<unknown>('/usuarios/me', { signal })
  const { id, nome, cpf, dataNascimento, email, perfil, ativo } = lerUsuario(data)
  const criadoEm = (data as Record<string, unknown>).criadoEm
  return { id, nome, cpf, dataNascimento, email, perfil, ativo, criadoEm: typeof criadoEm === 'string' ? criadoEm : null }
}

/** Exclusivo do Gestor (RF23). O backend gera a senha inicial e a devolve uma única vez. */
export async function cadastrarSupervisor(dados: DadosPessoa): Promise<UsuarioCriado> {
  const { data } = await http.post<unknown>('/usuarios/supervisores', dados)
  return lerUsuario(data)
}

/** Supervisor ou Gestor. Não envia senha nem perfil: o backend recusa esses campos com 400. */
export async function cadastrarColaborador(dados: DadosPessoa): Promise<UsuarioCriado> {
  const { data } = await http.post<unknown>('/usuarios/colaboradores', dados)
  return lerUsuario(data)
}

/** Dados editáveis na atualização (issue #122). CPF identifica o cadastro e não muda por aqui. */
export type DadosAtualizacao = Omit<DadosPessoa, 'cpf'>

/** Exclusivo do Gestor. Preserva CPF, perfil e credenciais; só dados cadastrais mudam. */
export async function atualizarSupervisor(id: number, dados: DadosAtualizacao): Promise<Usuario> {
  const { data } = await http.put<unknown>(`/usuarios/supervisores/${id}`, dados)
  return lerUsuario(data)
}

/** Supervisor ou Gestor. O Colaborador continua sem conta de acesso. */
export async function atualizarColaborador(id: number, dados: DadosAtualizacao): Promise<Usuario> {
  const { data } = await http.put<unknown>(`/usuarios/colaboradores/${id}`, dados)
  return lerUsuario(data)
}

const dadosRecusados = usarMensagemDaApi(
  'Os dados informados conflitam com um cadastro existente ou são inválidos.')

export const descreverFalhaDeCadastro = criarDescritorDeFalha({
  400: dadosRecusados,
  403: 'Seu perfil não tem permissão para esta operação.',
  404: 'Cadastro não encontrado. Ele pode ter sido alterado por outra pessoa; recarregue a lista.',
  409: dadosRecusados,
}, 'Não foi possível concluir a operação agora. Os dados preenchidos foram mantidos; tente novamente.')
