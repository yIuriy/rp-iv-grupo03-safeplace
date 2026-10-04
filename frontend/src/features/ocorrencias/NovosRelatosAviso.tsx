// Painel de novos relatos do Gestor de Segurança (UC09, passo 12): mostra os relatos que aguardam
// triagem, os mais recentes primeiro, com atalho para abrir o detalhe e conduzir a triagem.

import { Badge, Button } from '../../shared/components'
import { formatarDataHora } from '../estoque/formatos'
import type { OcorrenciaResumo } from './api'
import { rotuloTipoOcorrencia } from './formatos'

const LIMITE = 5

export function NovosRelatosAviso({ relatos, aoAbrir }: { relatos: OcorrenciaResumo[]; aoAbrir: (id: number) => void }) {
  if (relatos.length === 0) return null
  const visiveis = relatos.slice(0, LIMITE)
  const titulo = relatos.length === 1 ? '1 novo relato aguardando triagem' : `${relatos.length} novos relatos aguardando triagem`

  return <section className="sp-alert sp-tone--warning sp-novos-relatos" aria-labelledby="novos-relatos-titulo">
    <div>
      <h2 id="novos-relatos-titulo" className="sp-alert-title" aria-live="polite">{titulo}</h2>
      <ul className="sp-novos-relatos-lista">
        {visiveis.map(relato => <li key={relato.idOcorrencia}>
          <span>
            <strong>#{relato.idOcorrencia}</strong>{' '}
            <Badge tone={relato.tipoOcorrencia === 'ACIDENTE' ? 'danger' : 'warning'}>{rotuloTipoOcorrencia[relato.tipoOcorrencia]}</Badge>{' '}
            {relato.areaNome ?? 'Área não vinculada'}
            {relato.dataRegistro && <span className="sp-texto-secundario"> · registrado em {formatarDataHora(relato.dataRegistro)}</span>}
          </span>
          <Button variant="secondary" onClick={() => aoAbrir(relato.idOcorrencia)}
            aria-label={`Abrir novo relato ${relato.idOcorrencia} para triagem`}>Abrir</Button>
        </li>)}
      </ul>
      {relatos.length > LIMITE && <p className="sp-texto-secundario">
        E mais {relatos.length - LIMITE}. Filtre a lista pela situação Aberta para ver todos.
      </p>}
    </div>
  </section>
}
