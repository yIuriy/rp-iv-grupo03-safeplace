import type { ReactNode } from 'react'
import { Alert, Button, EmptyState } from '../../shared/components'
import type { Consulta } from '../../shared/api/useConsulta'

/** Estados de uma listagem: erro com nova tentativa, carregando, vazia ou com dados. */
export function ResultadoConsulta<T>({ consulta, carregandoLabel, erroTitulo, vazio, children }: {
  consulta: Consulta<T[]>
  carregandoLabel: string
  erroTitulo: string
  vazio: { title: string; description: string }
  children: (itens: T[]) => ReactNode
}) {
  if (consulta.erro) {
    return <>
      <Alert tone="danger" live="assertive" title={erroTitulo}>
        Verifique sua conexão ou tente novamente em alguns instantes.
      </Alert>
      <div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div>
    </>
  }
  if (consulta.dados === null) return <p role="status">{carregandoLabel}</p>
  if (consulta.dados.length === 0) return <EmptyState title={vazio.title} description={vazio.description} />
  return <>{children(consulta.dados)}</>
}
