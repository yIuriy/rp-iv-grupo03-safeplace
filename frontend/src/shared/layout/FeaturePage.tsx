import type { ReactNode } from 'react'
import { EmptyState } from '../components'

export function FeaturePage({ title, description, children }: {
  title: string
  description: string
  children?: ReactNode
}) {
  return <section className="sp-feature-page">
    <header className="sp-feature-heading">
      <h1 className="sp-type-heading-md">{title}</h1>
      <p>{description}</p>
    </header>
    {children ?? <EmptyState title="Funcionalidade em desenvolvimento"
      description="Esta tela está em preparação. As operações estarão disponíveis em uma próxima entrega." />}
  </section>
}
