import { http } from '../../shared/api/https'
import { lerSessaoDe, type Sessao } from './sessao'

/** `POST /api/auth/login`: o backend aceita apenas e-mail e senha e devolve token, nome e perfil. */
export async function autenticar(email: string, senha: string): Promise<Sessao> {
  const { data } = await http.post<unknown>('/auth/login', { email, senha })
  return lerSessaoDe(data)
}
