import { Badge, Button, Table, TableRow } from '../../shared/components'
import type { Usuario } from './api'
import { formatarCpf, formatarData } from './formatos'

/** Mostra só dados cadastrais. Credenciais nunca chegam aqui: a API não as devolve em listagens. */
export function UsuariosTabela({ caption, usuarios, aoEditar }: {
  caption: string
  usuarios: Usuario[]
  aoEditar: (usuario: Usuario) => void
}) {
  return <Table caption={caption}>
    <thead><tr>
      <th scope="col">Nome</th><th scope="col">CPF</th><th scope="col">Nascimento</th>
      <th scope="col">E-mail</th><th scope="col">Situação</th><th scope="col">Ações</th>
    </tr></thead>
    <tbody>{usuarios.map(usuario => <TableRow key={usuario.id}>
      <td><strong>{usuario.nome}</strong></td>
      <td>{formatarCpf(usuario.cpf)}</td>
      <td>{formatarData(usuario.dataNascimento)}</td>
      <td>{usuario.email ?? 'Não informado'}</td>
      <td><Badge tone={usuario.ativo ? 'success' : 'warning'}>{usuario.ativo ? 'Ativo' : 'Inativo'}</Badge></td>
      <td><Button variant="secondary" onClick={() => aoEditar(usuario)} aria-label={`Editar ${usuario.nome}`}>Editar</Button></td>
    </TableRow>)}</tbody>
  </Table>
}
