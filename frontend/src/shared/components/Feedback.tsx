import type { ReactNode } from 'react'
import { Icon, type IconName } from './Icon'
import { IconButton } from './Button'

export type FeedbackTone = 'info' | 'success' | 'warning' | 'danger'
const toneIcons: Record<FeedbackTone, IconName> = {
  info: 'info', success: 'check-circle', warning: 'alert-triangle', danger: 'alert-triangle',
}

export function Badge({ tone = 'info', children }: { tone?: FeedbackTone; children: ReactNode }) {
  return <span className={`sp-badge sp-tone--${tone}`}>{children}</span>
}

export function Alert({ tone = 'info', title, children, live = 'off' }: {
  tone?: FeedbackTone
  title: string
  children: ReactNode
  live?: 'off' | 'polite' | 'assertive'
}) {
  return <div className={`sp-alert sp-tone--${tone}`}
    role={live === 'assertive' ? 'alert' : live === 'polite' ? 'status' : undefined}>
    <Icon name={toneIcons[tone]} />
    <div><p className="sp-alert-title">{title}</p><div className="sp-alert-body">{children}</div></div>
  </div>
}

export function Toast({ message, tone = 'success', onDismiss }: {
  message: string
  tone?: FeedbackTone
  onDismiss: () => void
}) {
  return <div className={message ? `sp-toast sp-tone--${tone}` : undefined}>
    <div className="sp-toast-message" role="status" aria-atomic="true">
      {message && <><Icon name={toneIcons[tone]} /><span>{message}</span></>}
    </div>
    {message && <IconButton variant="quiet" label="Fechar aviso" icon="x" onClick={onDismiss} />}
  </div>
}
