import type { ComponentProps } from 'react'
import { Icon, type IconName } from './Icon'

export type ButtonProps = ComponentProps<'button'> & {
  variant?: 'primary' | 'secondary' | 'quiet' | 'danger'
  icon?: IconName
  loading?: boolean
}

export function Button({ variant = 'primary', icon, loading = false,
  disabled, type = 'button', className = '', children, ...props }: ButtonProps) {
  return <button {...props} type={type}
    className={`sp-button sp-button--${variant} ${className}`}
    disabled={disabled || loading} aria-busy={loading || undefined}>
    {(loading || icon) && <Icon name={loading ? 'loader' : icon!}
      className={loading ? 'sp-spinner' : ''} />}
    {children}
  </button>
}

export function IconButton({ label, icon, ...props }: Omit<ButtonProps, 'children' | 'icon'> & {
  label: string
  icon: IconName
}) {
  return <Button variant="secondary" {...props} icon={icon}
    className={`sp-icon-button ${props.className ?? ''}`} aria-label={label} />
}
