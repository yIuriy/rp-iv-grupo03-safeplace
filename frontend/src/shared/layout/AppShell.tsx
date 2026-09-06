import { useEffect, useId, useState, type ReactNode, type ComponentProps } from 'react'
import logo from '../../../design-system/assets/logo-primary.svg'
import { Button, Dialog, Icon, type IconName } from '../components'

export function NavItem({ icon, active, children, className = '', ...props }: ComponentProps<'a'> & {
  icon: IconName
  active?: boolean
}) {
  return <a {...props} aria-current={active ? 'page' : undefined}
    className={`sp-nav-item ${className}`}><Icon name={icon} /><span>{children}</span></a>
}

export function AppHeader({ roleLabel, onMenuClick, menuOpen, menuId }: {
  roleLabel: string
  onMenuClick: () => void
  menuOpen: boolean
  menuId: string
}) {
  return <header className="sp-app-header">
    <Button variant="quiet" className="sp-menu-button" onClick={onMenuClick}
      aria-expanded={menuOpen} aria-controls={menuId} aria-haspopup="dialog">Menu</Button>
    <a href="/" className="sp-logo-link" aria-label="SafePlace, início"><img src={logo} alt="" className="sp-logo" /></a>
    <span className="sp-role-label">{roleLabel}</span>
  </header>
}

export function AppShell({ roleLabel, navigation, children }: {
  roleLabel: string
  navigation: ReactNode
  children: ReactNode
}) {
  const [menuOpen, setMenuOpen] = useState(false)
  const id = useId()

  useEffect(() => {
    const desktop = window.matchMedia('(min-width: 1200px)')
    const closeOnDesktop = () => { if (desktop.matches) setMenuOpen(false) }
    desktop.addEventListener('change', closeOnDesktop)
    return () => desktop.removeEventListener('change', closeOnDesktop)
  }, [])

  return <div className="sp-app-shell">
    <a href="#main-content" className="sp-skip-link">Pular para o conteúdo</a>
    <AppHeader {...{ roleLabel, menuOpen }} menuId={id} onMenuClick={() => setMenuOpen(true)} />
    <div className="sp-app-body">
      <aside className="sp-sidebar"><nav aria-label="Navegação principal">{navigation}</nav></aside>
      <main id="main-content" className="sp-main" tabIndex={-1}>{children}</main>
    </div>
    <Dialog open={menuOpen} title="Navegação" onClose={() => setMenuOpen(false)} className="sp-navigation-dialog">
      <div id={id}>
        <nav aria-label="Navegação principal" onClick={event => {
          if ((event.target as HTMLElement).closest('a')) setMenuOpen(false)
        }}>{navigation}</nav>
      </div>
      <Button variant="secondary" onClick={() => setMenuOpen(false)}>Fechar menu</Button>
    </Dialog>
  </div>
}
