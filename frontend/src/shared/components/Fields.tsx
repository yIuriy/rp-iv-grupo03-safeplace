import { useId, type ComponentProps, type ReactNode } from 'react'
import { Icon } from './Icon'

type FieldProps = {
  label: string
  helper?: string
  error?: string
  className?: string
}

function useFieldIds(id?: string, helper?: string, error?: string, describedBy?: string) {
  const generatedId = useId()
  const controlId = id ?? generatedId
  const messageId = `${controlId}-message`
  return {
    id: controlId,
    messageId,
    'aria-describedby': [describedBy, (error || helper) && messageId].filter(Boolean).join(' ') || undefined,
  }
}

function Field({ label, helper, error, id, messageId, className = '', hiddenLabel, children }: FieldProps & {
  id: string
  messageId: string
  hiddenLabel?: boolean
  children: ReactNode
}) {
  return <div className={`sp-field ${className}`}>
    <label className={hiddenLabel ? 'sp-sr-only' : 'sp-field-label'} htmlFor={id}>{label}</label>
    {children}
    {(error || helper) && <p id={messageId} className={`sp-field-message ${error ? 'sp-field-message--error' : ''}`}>
      {error || helper}
    </p>}
  </div>
}

export function TextField({ label, helper, error, className, ...props }: FieldProps & ComponentProps<'input'>) {
  const ids = useFieldIds(props.id, helper, error, props['aria-describedby'])
  return <Field {...{ label, helper, error, className, ...ids }}>
    <input {...props} id={ids.id} aria-describedby={ids['aria-describedby']}
      aria-invalid={error ? true : props['aria-invalid']} className="sp-control" />
  </Field>
}

export function TextArea({ label, helper, error, className, ...props }: FieldProps & ComponentProps<'textarea'>) {
  const ids = useFieldIds(props.id, helper, error, props['aria-describedby'])
  return <Field {...{ label, helper, error, className, ...ids }}>
    <textarea {...props} id={ids.id} aria-describedby={ids['aria-describedby']}
      aria-invalid={error ? true : props['aria-invalid']} className="sp-control sp-textarea" />
  </Field>
}

export function Select({ label, helper, error, className, children, ...props }: FieldProps & ComponentProps<'select'>) {
  const ids = useFieldIds(props.id, helper, error, props['aria-describedby'])
  return <Field {...{ label, helper, error, className, ...ids }}>
    <div className="sp-select-wrap">
      <select {...props} id={ids.id} aria-describedby={ids['aria-describedby']}
        aria-invalid={error ? true : props['aria-invalid']} className="sp-control sp-select">{children}</select>
      <Icon name="chevron-down" />
    </div>
  </Field>
}

export function SearchInput({ label, helper, error, className, hiddenLabel = true, ...props }:
  FieldProps & Omit<ComponentProps<'input'>, 'type'> & { hiddenLabel?: boolean }) {
  const ids = useFieldIds(props.id, helper, error, props['aria-describedby'])
  return <Field {...{ label, helper, error, className, hiddenLabel, ...ids }}>
    <div className="sp-search-wrap">
      <Icon name="search" />
      <input {...props} type="search" id={ids.id} aria-describedby={ids['aria-describedby']}
        aria-invalid={error ? true : props['aria-invalid']} className="sp-control sp-search" />
    </div>
  </Field>
}

type ChoiceProps = Omit<ComponentProps<'input'>, 'type' | 'children'> & { label: string }

function Choice({ kind, label, className = '', ...props }: ChoiceProps & { kind: 'checkbox' | 'radio' | 'toggle' }) {
  return <label className={`sp-choice sp-choice--${kind} ${className}`}>
    <span className="sp-choice-mark">
      <input {...props} type={kind === 'radio' ? 'radio' : 'checkbox'}
        role={kind === 'toggle' ? 'switch' : undefined} className="sp-choice-input" />
      <span className="sp-choice-visual" aria-hidden="true">
        {kind === 'checkbox' && <Icon name="check" />}
      </span>
    </span>
    <span>{label}</span>
  </label>
}

export function Checkbox(props: ChoiceProps) { return <Choice {...props} kind="checkbox" /> }
export function Radio(props: ChoiceProps) { return <Choice {...props} kind="radio" /> }
export function Toggle(props: ChoiceProps) { return <Choice {...props} kind="toggle" /> }

export function FileUpload(props: FieldProps & Omit<ComponentProps<'input'>, 'type'>) {
  return <TextField {...props} type="file" className={`sp-file-field ${props.className ?? ''}`} />
}
