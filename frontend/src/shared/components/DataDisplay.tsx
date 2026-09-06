import { useId, type ComponentProps, type ReactNode } from 'react'
import { Icon } from './Icon'
import { Button } from './Button'
import { Badge, type FeedbackTone } from './Feedback'

export function Table({ caption, children, ...props }: ComponentProps<'table'> & { caption: string }) {
  const id = useId()
  return <div className="sp-table-region" role="region" aria-labelledby={id} tabIndex={0}>
    <table {...props} className={`sp-table ${props.className ?? ''}`}>
      <caption id={id} className="sp-sr-only">{caption}</caption>
      {children}
    </table>
  </div>
}

export function TableRow(props: ComponentProps<'tr'>) {
  return <tr {...props} className={`sp-table-row ${props.className ?? ''}`} />
}

export function EmptyState({ title, description, actionLabel, onAction }: {
  title: string
  description: string
  actionLabel?: string
  onAction?: () => void
}) {
  return <div className="sp-empty-state">
    <Icon name="search" />
    <div><h2 className="sp-type-heading-sm">{title}</h2><p>{description}</p></div>
    {actionLabel && onAction && <Button variant="secondary" onClick={onAction}>{actionLabel}</Button>}
  </div>
}

export function HistoryItem({ dateTime, dateLabel, title, children }: {
  dateTime: string
  dateLabel: string
  title: string
  children: ReactNode
}) {
  return <li className="sp-history-item">
    <Icon name="check-circle" />
    <div><time dateTime={dateTime}>{dateLabel}</time><h3>{title}</h3><p>{children}</p></div>
  </li>
}

export function RiskAreaCard({ title, location, hazards, ppe, riskLabel, riskTone, onConsult }: {
  title: string
  location: string
  hazards: string
  ppe: string
  riskLabel: string
  riskTone: FeedbackTone
  onConsult: () => void
}) {
  const id = useId()
  return <article className="sp-risk-card" aria-labelledby={id}>
    <div className="sp-risk-summary">
      <div className="sp-risk-heading"><h2 id={id} className="sp-type-heading-sm">{title}</h2>
        <Badge tone={riskTone}>{riskLabel}</Badge></div>
      <p className="sp-risk-location">{location}</p>
      <dl className="sp-risk-facts">
        <div><dt>Perigos mapeados</dt><dd>{hazards}</dd></div>
        <div><dt>EPIs vinculados</dt><dd>{ppe}</dd></div>
      </dl>
    </div>
    <Button variant="secondary" className="sp-risk-action" onClick={onConsult}
      aria-label={`Consultar área: ${title}`}>Consultar área</Button>
  </article>
}
