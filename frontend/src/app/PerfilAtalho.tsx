// Atalho para a tela de perfil no rodapé da barra lateral: iniciais, nome e perfil da pessoa
// logada, lidos da sessão, com link para /perfil.

import { NavLink } from 'react-router-dom'
import { useSessaoAtiva } from '../features/autenticacao/contexto'
import { rotuloDoPerfil } from '../features/autenticacao/sessao'

/** Primeira letra do primeiro e do último nome: "Sara de Lima" vira "SL". */
function iniciaisDoNome(nome: string): string {
  const partes = nome.trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) return '?'
  const primeira = partes[0][0]
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : ''
  return (primeira + ultima).toUpperCase()
}

export function PerfilAtalho() {
  const sessao = useSessaoAtiva()
  return <NavLink to="/perfil" className="sp-nav-item sp-nav-perfil">
    <span className="sp-avatar" aria-hidden="true">{iniciaisDoNome(sessao.nome)}</span>
    <span className="sp-nav-perfil-texto">
      <span className="sp-sr-only">Meu perfil: </span>
      <span className="sp-nav-perfil-nome">{sessao.nome}</span>{' '}
      <span className="sp-nav-perfil-papel">{rotuloDoPerfil[sessao.perfil]}</span>
    </span>
  </NavLink>
}
