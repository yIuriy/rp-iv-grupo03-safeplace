import { http } from '../../shared/api/https'
import { mensagemDaApi, statusDoErro } from '../../shared/api/erros'

export type PerfilUsuario = 'COLABORADOR' | 'SUPERVISOR' | 'GESTOR_SEGURANCA'

export type Usuario = {
  id: number
  nome: string
  cpf: string
  dataNascimento: string
  email: string | null
  perfil: PerfilUsuario
  ativo: boolean
}

/** Resposta de criação. `senhaInicial` só vem no cadastro de Supervisor e nunca em listagens. */
export type UsuarioCriado = Usuario & { senhaInicial?: string }

export type DadosPessoa = { nome: string; cpf: string; dataNascimento: string; email?: string }

export type FiltroColaboradores = { nome?: string; cpf?: string }

const perfis: readonly PerfilUsuario[] = ['COLABORADOR', 'SUPERVISOR', 'GESTOR_SEGURANCA']

function ehPerfil(valor: unknown): valor is PerfilUsuario {
  return perfis.includes(valor as PerfilUsuario)
}

function lerUsuario(dado: unknown): UsuarioCriado {
  if (dado === null || typeof dado !== 'object') throw new Error('Registro de usuário inválido')
  const bruto = dado as Record<string, unknown>
  if (typeof bruto.id !== 'number' || typeof bruto.nome !== 'string' || typeof bruto.cpf !== 'string'
    || typeof bruto.dataNascimento !== 'string' || !ehPerfil(bruto.perfil)) {
    throw new Error('Registro de usuário incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    nome: bruto.nome,
    cpf: bruto.cpf,
    dataNascimento: bruto.dataNascimento,
    email: typeof bruto.email === 'string' ? bruto.email : null,
    perfil: bruto.perfil,
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

export function descreverFalhaDeCadastro(erro: unknown): string {
  const status = statusDoErro(erro)
  if (status === 403) return 'Seu perfil não tem permissão para este cadastro.'
  if (status === 409 || status === 400) {
    return mensagemDaApi(erro) ?? 'Os dados informados conflitam com um cadastro existente ou são inválidos.'
  }
  return 'Não foi possível concluir o cadastro agora. Os dados preenchidos foram mantidos; tente novamente.'
}
