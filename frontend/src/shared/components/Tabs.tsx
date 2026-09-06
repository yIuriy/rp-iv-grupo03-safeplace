import { useId, useRef, type ReactNode } from 'react'

export function Tabs({ label, items, value, onValueChange }: {
  label: string
  items: { id: string; label: string; content: ReactNode }[]
  value: string
  onValueChange: (value: string) => void
}) {
  const id = useId()
  const refs = useRef<(HTMLButtonElement | null)[]>([])
  return <div className="sp-tabs">
    <div className="sp-tab-list" role="tablist" aria-label={label}>
      {items.map((item, index) => <button key={item.id} type="button" role="tab"
        id={`${id}-tab-${item.id}`} aria-controls={`${id}-panel-${item.id}`}
        aria-selected={value === item.id} tabIndex={value === item.id ? 0 : -1}
        className="sp-tab" ref={element => { refs.current[index] = element }}
        onClick={() => onValueChange(item.id)}
        onKeyDown={event => {
          let next: number
          if (event.key === 'ArrowRight') next = (index + 1) % items.length
          else if (event.key === 'ArrowLeft') next = (index - 1 + items.length) % items.length
          else if (event.key === 'Home') next = 0
          else if (event.key === 'End') next = items.length - 1
          else return
          event.preventDefault()
          onValueChange(items[next].id)
          refs.current[next]?.focus()
        }}>{item.label}</button>)}
    </div>
    {items.map(item => <div key={item.id} role="tabpanel" className="sp-tab-panel"
      id={`${id}-panel-${item.id}`} aria-labelledby={`${id}-tab-${item.id}`}
      hidden={value !== item.id} tabIndex={0}>{item.content}</div>)}
  </div>
}
