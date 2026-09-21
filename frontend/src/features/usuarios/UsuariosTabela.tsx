// Tabela de pessoas cadastradas: nome, CPF oculto, nascimento, e-mail, situação e botão Editar.
// Nunca exibe credenciais.

import { Badge, Button, Table, TableRow } from '../../shared/components'
import type { Usuario } from './api'
import { CpfOculto } from './CpfOculto'
import { formatarData } from './formatos'

/**
 * Mostra só dados cadastrais, com o CPF coberto por padrão: a listagem serve para localizar o cadastro,
 * e o número completo aparece só quando alguém pede para ver. Credenciais nunca chegam aqui: a API não as devolve em listagens.
 */
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
      <td><CpfOculto cpf={usuario.cpf} nome={usuario.nome} /></td>
      <td>{formatarData(usuario.dataNascimento)}</td>
      <td>{usuario.email ?? 'Não informado'}</td>
      <td><Badge tone={usuario.ativo ? 'success' : 'warning'}>{usuario.ativo ? 'Ativo' : 'Inativo'}</Badge></td>
      <td><Button variant="secondary" onClick={() => aoEditar(usuario)} aria-label={`Editar ${usuario.nome}`}>Editar</Button></td>
    </TableRow>)}</tbody>
  </Table>
}
