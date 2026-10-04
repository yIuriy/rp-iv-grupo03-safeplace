// Layout das telas autenticadas: menu entre as funcionalidades, título da aba, foco no conteúdo ao
// trocar de tela, nome e perfil da pessoa logada, botão Sair e, para o Gestor de Segurança, o
// contador de novos relatos aguardando triagem no item Ocorrências (UC09, passo 12).

import { useEffect, useRef } from 'react'
import { matchPath, NavLink, Outlet, useLocation } from 'react-router-dom'
import { AppShell } from '../shared/layout'
import { Button, Icon, type IconName } from '../shared/components'
import { useAutenticacao, useSessaoAtiva } from '../features/autenticacao/contexto'
import { rotuloDoPerfil } from '../features/autenticacao/sessao'
import { useNovosRelatos } from '../features/ocorrencias/useNovosRelatos'
import './app.css'

const navigation: { path: string; label: string; icon: IconName }[] = [
  { path: '/usuarios', label: 'Usuários', icon: 'users' },
  { path: '/capacitacoes', label: 'Capacitações', icon: 'check' },
  { path: '/ocorrencias', label: 'Ocorrências', icon: 'clipboard' },
  { path: '/epis', label: 'EPIs', icon: 'package' },
  { path: '/emprestimos', label: 'Empréstimos de EPIs', icon: 'calendar' },
  { path: '/areas-risco', label: 'Áreas de risco', icon: 'map-pin' },
  { path: '/tarefas', label: 'Tarefas', icon: 'check-circle' },
]

export function AppLayout() {
  const { pathname, search } = useLocation()
  const previousPath = useRef(pathname)
  const { sair } = useAutenticacao()
  const sessao = useSessaoAtiva()
  const novosRelatos = useNovosRelatos(sessao.perfil === 'GESTOR_SEGURANCA').relatos.length

  useEffect(() => {
    const previousTitle = document.title
    const currentPage = navigation.find(item => matchPath(item.path, pathname))
    const titulo = pathname === '/epis'
      ? `EPIs — ${new URLSearchParams(search).get('aba') === 'manutencao' ? 'Manutenção' : 'Catálogo e estoque'}`
      : currentPage?.label ?? 'Página não encontrada'
    document.title = `${titulo} | SafePlace`
    if (previousPath.current !== pathname) {
      document.getElementById('main-content')?.focus()
      previousPath.current = pathname
    }
    return () => { document.title = previousTitle }
  }, [pathname, search])

  return <AppShell roleLabel={`${sessao.nome} · ${rotuloDoPerfil[sessao.perfil]}`}
    actions={<>
      {novosRelatos > 0 && <span id="nav-novos-relatos" hidden>
        {novosRelatos === 1 ? '1 novo relato aguardando triagem' : `${novosRelatos} novos relatos aguardando triagem`}
      </span>}
      <Button variant="quiet" onClick={sair}>Sair</Button>
    </>}
    navigation={navigation.map(item => {
      const contador = item.path === '/ocorrencias' && novosRelatos > 0 ? novosRelatos : 0
      return <NavLink key={item.path} to={item.path} className="sp-nav-item"
        aria-describedby={contador ? 'nav-novos-relatos' : undefined}>
        <Icon name={item.icon} /><span>{item.label}</span>
        {contador > 0 && <span className="sp-nav-contador" aria-hidden="true">{contador}</span>}
      </NavLink>
    })}>
    <Outlet />
  </AppShell>
}
