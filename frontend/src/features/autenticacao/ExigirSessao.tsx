import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAutenticacao } from './contexto'

/** Rota de layout: sem sessão, envia ao login guardando o destino para voltar depois. */
export function ExigirSessao() {
  const { sessao } = useAutenticacao()
  const { pathname, search } = useLocation()
  if (!sessao) return <Navigate to="/login" replace state={{ de: pathname + search }} />
  return <Outlet />
}
