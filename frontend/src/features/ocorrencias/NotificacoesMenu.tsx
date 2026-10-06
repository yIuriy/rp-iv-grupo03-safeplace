// Menu flutuante de notificações no canto superior direito:
// Apresenta ao Gestor de Segurança os relatos recém-criados que aguardam triagem (UC09, DS09).
// Exibe badge com contagem e atalho para abrir o relato diretamente.

import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Badge, Button, Icon } from '../../shared/components'
import { formatarDataHora } from '../estoque/formatos'
import type { OcorrenciaResumo } from './api'
import { rotuloTipoOcorrencia } from './formatos'
import './notificacoes.css'

const LIMITE_MENU = 5

export function NotificacoesMenu({ relatos }: { relatos: OcorrenciaResumo[] }) {
  const [aberto, setAberto] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)
  const botaoRef = useRef<HTMLButtonElement>(null)
  const navigate = useNavigate()

  const total = relatos.length
  const rotuloBotao = total === 0
    ? 'Notificações'
    : total === 1
      ? 'Notificações: 1 novo relato aguardando triagem'
      : `Notificações: ${total} novos relatos aguardando triagem`

  useEffect(() => {
    if (!aberto) return

    function cliqueFora(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setAberto(false)
      }
    }

    function teclaEsc(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setAberto(false)
        botaoRef.current?.focus()
      }
    }

    document.addEventListener('mousedown', cliqueFora)
    document.addEventListener('keydown', teclaEsc)
    return () => {
      document.removeEventListener('mousedown', cliqueFora)
      document.removeEventListener('keydown', teclaEsc)
    }
  }, [aberto])

  function abrirRelato(id: number) {
    setAberto(false)
    navigate(`/ocorrencias?ocorrencia=${id}`)
  }

  function verTodas() {
    setAberto(false)
    navigate('/ocorrencias')
  }

  const visiveis = relatos.slice(0, LIMITE_MENU)

  return (
    <div className="sp-notificacoes-container" ref={menuRef}>
      <button
        ref={botaoRef}
        type="button"
        className="sp-notificacoes-botao"
        onClick={() => setAberto(v => !v)}
        aria-label={rotuloBotao}
        aria-haspopup="dialog"
        aria-expanded={aberto}
      >
        <Icon name="bell" />
        {total > 0 && (
          <span className="sp-notificacoes-badge" aria-hidden="true">
            {total > 99 ? '99+' : total}
          </span>
        )}
      </button>

      {aberto && (
        <div
          className="sp-notificacoes-popover"
          role="dialog"
          aria-label="Painel de notificações"
        >
          <header className="sp-notificacoes-header">
            <h3 className="sp-notificacoes-titulo">Notificações</h3>
            <span className="sp-notificacoes-subtitulo">
              {total === 0
                ? 'Nenhum novo relato aguardando triagem'
                : total === 1
                  ? '1 novo relato aguardando triagem'
                  : `${total} novos relatos aguardando triagem`}
            </span>
          </header>

          <div className="sp-notificacoes-corpo">
            {total === 0 ? (
              <p className="sp-notificacoes-vazio">Nenhuma notificação no momento.</p>
            ) : (
              <ul className="sp-notificacoes-lista">
                {visiveis.map(relato => (
                  <li key={relato.idOcorrencia} className="sp-notificacoes-item">
                    <div className="sp-notificacoes-item-cabecalho">
                      <strong>#{relato.idOcorrencia}</strong>
                      <Badge tone={relato.tipoOcorrencia === 'ACIDENTE' ? 'danger' : 'warning'}>
                        {rotuloTipoOcorrencia[relato.tipoOcorrencia]}
                      </Badge>
                    </div>
                    <p className="sp-notificacoes-item-descricao">{relato.descricao}</p>
                    <div className="sp-notificacoes-item-meta">
                      <span>{relato.areaNome ?? 'Área não vinculada'}</span>
                      {relato.dataRegistro && (
                        <span> · {formatarDataHora(relato.dataRegistro)}</span>
                      )}
                    </div>
                    <Button
                      variant="secondary"
                      className="sp-notificacoes-item-acao"
                      onClick={() => abrirRelato(relato.idOcorrencia)}
                      aria-label={`Abrir relato #${relato.idOcorrencia} para triagem`}
                    >
                      Abrir triagem
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {total > 0 && (
            <footer className="sp-notificacoes-rodape">
              <Button variant="quiet" onClick={verTodas}>
                Ver todas as ocorrências
              </Button>
            </footer>
          )}
        </div>
      )}
    </div>
  )
}
