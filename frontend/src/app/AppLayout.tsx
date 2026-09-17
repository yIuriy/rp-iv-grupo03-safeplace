import { useEffect, useRef } from 'react'
import { matchPath, NavLink, Outlet, useLocation } from 'react-router-dom'
import { AppShell } from '../shared/layout'
import { Button, Icon, type IconName } from '../shared/components'
import { useAutenticacao, useSessaoAtiva } from '../features/autenticacao/contexto'
import { rotuloDoPerfil } from '../features/autenticacao/sessao'
import './app.css'

const navigation: { path: string; label: string; icon: IconName }[] = [
  { path: '/usuarios', label: 'Usuários', icon: 'users' },
  { path: '/ocorrencias', label: 'Ocorrências', icon: 'clipboard' },
  { path: '/epis', label: 'EPIs', icon: 'package' },
  { path: '/areas-risco', label: 'Áreas de risco', icon: 'map-pin' },
  { path: '/tarefas', label: 'Tarefas', icon: 'check-circle' },
]

export function AppLayout() {
  const { pathname } = useLocation()
  const previousPath = useRef(pathname)
  const { sair } = useAutenticacao()
  const sessao = useSessaoAtiva()

  useEffect(() => {
    const previousTitle = document.title
    const currentPage = navigation.find(item => matchPath(item.path, pathname))
    document.title = `${currentPage?.label ?? 'Página não encontrada'} | SafePlace`
    if (previousPath.current !== pathname) {
      document.getElementById('main-content')?.focus()
      previousPath.current = pathname
    }
    return () => { document.title = previousTitle }
  }, [pathname])

  return <AppShell roleLabel={`${sessao.nome} · ${rotuloDoPerfil[sessao.perfil]}`}
    actions={<Button variant="quiet" onClick={sair}>Sair</Button>}
    navigation={navigation.map(item =>
      <NavLink key={item.path} to={item.path} className="sp-nav-item">
        <Icon name={item.icon} /><span>{item.label}</span>
      </NavLink>
    )}>
    <Outlet />
  </AppShell>
}
