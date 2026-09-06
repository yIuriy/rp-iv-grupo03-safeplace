import { useEffect, useId, useRef, type ReactNode } from 'react'
import { Button } from './Button'

export function Dialog({ open, title, description, onClose, children, className = '' }: {
  open: boolean
  title: string
  description?: string
  onClose: () => void
  children: ReactNode
  className?: string
}) {
  const ref = useRef<HTMLDialogElement>(null)
  const id = useId()

  useEffect(() => {
    if (!open) return
    const dialog = ref.current!
    const opener = document.activeElement instanceof HTMLElement ? document.activeElement : null
    dialog.showModal()
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      dialog.close()
      document.body.style.overflow = previousOverflow
      opener?.focus()
    }
  }, [open])

  return <dialog ref={ref} className={`sp-dialog ${className}`}
    aria-labelledby={`${id}-title`} aria-describedby={description ? `${id}-description` : undefined}
    onKeyDown={event => {
      if (event.key !== 'Tab') return
      const dialog = event.currentTarget
      const controls = Array.from(dialog.querySelectorAll<HTMLElement>('button, a[href], input, select, textarea, [tabindex]'))
        .filter(element => element.tabIndex >= 0 && !element.matches(':disabled') && element.getClientRects().length > 0)
      const first = controls[0]
      const last = controls[controls.length - 1]
      if (!first) {
        event.preventDefault()
        dialog.focus()
      } else if (event.shiftKey && (document.activeElement === first || document.activeElement === dialog)) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }}
    onCancel={event => { event.preventDefault(); onClose() }}>
    <h2 id={`${id}-title`} className="sp-type-heading-md">{title}</h2>
    {description && <p id={`${id}-description`} className="sp-dialog-description">{description}</p>}
    {children}
  </dialog>
}

export function ConfirmDialog({ open, title, description, confirmLabel,
  cancelLabel = 'Continuar editando', loading = false, onConfirm, onClose }: {
  open: boolean
  title: string
  description: string
  confirmLabel: string
  cancelLabel?: string
  loading?: boolean
  onConfirm: () => void
  onClose: () => void
}) {
  return <Dialog {...{ open, title, description }} onClose={() => { if (!loading) onClose() }}>
    <div className="sp-dialog-actions">
      <Button variant="secondary" disabled={loading} onClick={onClose}>{cancelLabel}</Button>
      <Button variant="danger" loading={loading} onClick={onConfirm}>{confirmLabel}</Button>
    </div>
  </Dialog>
}
