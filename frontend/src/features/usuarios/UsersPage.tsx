import { useEffect, useState } from 'react'
import { http } from '../../shared/api/https'
import { Alert, Button, EmptyState, Table, TableRow } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'

type User = { id: number | string; nome: string }

function readUsers(data: unknown): User[] {
  if (!Array.isArray(data)) throw new Error('Expected a user list')
  return data.map(user => {
    if (user === null || typeof user !== 'object'
      || (typeof user.id !== 'number' && typeof user.id !== 'string')
      || typeof user.nome !== 'string') {
      throw new Error('Expected a user with id and nome')
    }
    return { id: user.id, nome: user.nome }
  })
}

export function UsersPage() {
  const [users, setUsers] = useState<User[] | null>(null)
  const [hasError, setHasError] = useState(false)
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    http.get<unknown>('/usuarios', { signal: controller.signal })
      .then(response => {
        if (!controller.signal.aborted) setUsers(readUsers(response.data))
      })
      .catch(() => {
        if (!controller.signal.aborted) setHasError(true)
      })
    return () => controller.abort()
  }, [attempt])

  function retry() {
    setHasError(false)
    setUsers(null)
    setAttempt(previous => previous + 1)
  }

  return <FeaturePage title="Usuários" description="Consulte os usuários com acesso ao SafePlace.">
    {hasError ? <>
      <Alert tone="danger" live="assertive" title="Não foi possível carregar os usuários.">
        Verifique sua conexão ou tente novamente em alguns instantes.
      </Alert>
      <div><Button variant="secondary" onClick={retry}>Tentar novamente</Button></div>
    </> : users === null ? <p role="status">Carregando usuários...</p>
      : users.length === 0 ? <EmptyState title="Nenhum usuário encontrado"
        description="Nenhum usuário foi retornado pela consulta." />
      : <Table caption="Usuários cadastrados">
      <thead><tr><th scope="col">Nome</th></tr></thead>
      <tbody>{users.map(user => <TableRow key={user.id}><td>{user.nome}</td></TableRow>)}</tbody>
    </Table>}
  </FeaturePage>
}
