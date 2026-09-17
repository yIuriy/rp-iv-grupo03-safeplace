import { Badge, Table, TableRow } from '../../shared/components'
import type { Usuario } from './api'
import { formatarCpf, formatarData } from './formatos'

/** Mostra só dados cadastrais. Credenciais nunca chegam aqui: a API não as devolve em listagens. */
export function UsuariosTabela({ caption, usuarios }: { caption: string; usuarios: Usuario[] }) {
  return <Table caption={caption}>
    <thead><tr>
      <th scope="col">Nome</th><th scope="col">CPF</th><th scope="col">Nascimento</th>
      <th scope="col">E-mail</th><th scope="col">Situação</th>
    </tr></thead>
    <tbody>{usuarios.map(usuario => <TableRow key={usuario.id}>
      <td><strong>{usuario.nome}</strong></td>
      <td>{formatarCpf(usuario.cpf)}</td>
      <td>{formatarData(usuario.dataNascimento)}</td>
      <td>{usuario.email ?? 'Não informado'}</td>
      <td><Badge tone={usuario.ativo ? 'success' : 'warning'}>{usuario.ativo ? 'Ativo' : 'Inativo'}</Badge></td>
    </TableRow>)}</tbody>
  </Table>
}
