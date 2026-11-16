// Painel de novos relatos do Gestor de Segurança (UC09, passo 12): mostra os relatos que aguardam
// triagem, os mais recentes primeiro, com atalho para abrir o detalhe e conduzir a triagem.

import { Badge, Button, Icon } from '../../shared/components'
import { formatarDataHora } from '../../shared/formatos'
import type { OcorrenciaResumo } from './api'
import { rotuloTipoOcorrencia } from './formatos'

const LIMITE = 5

export function NovosRelatosAviso({ relatos, aoAbrir }: { relatos: OcorrenciaResumo[]; aoAbrir: (id: number) => void }) {
  if (relatos.length === 0) return null
  const visiveis = relatos.slice(0, LIMITE)
  const titulo = relatos.length === 1 ? '1 novo relato aguardando triagem' : `${relatos.length} novos relatos aguardando triagem`

  return <section className="sp-novos-relatos" aria-labelledby="novos-relatos-titulo">
    <div>
      <h2 id="novos-relatos-titulo" className="sp-novos-relatos-titulo" aria-live="polite">{titulo}</h2>
      <ul className="sp-novos-relatos-lista">
        {visiveis.map(relato => {
          const tom = relato.tipoOcorrencia === 'ACIDENTE' ? 'danger' : 'warning'
          return <li key={relato.idOcorrencia} className="sp-novo-relato">
            <span className={`sp-novo-relato-icone sp-tone--${tom}`}><Icon name="alert-triangle" /></span>
            <span className="sp-novo-relato-texto">
              <strong>{relato.areaNome ?? 'Área não vinculada'}</strong>
              <span className="sp-novo-relato-detalhe">
                <Badge tone={tom}>{rotuloTipoOcorrencia[relato.tipoOcorrencia]}</Badge>
                <span className="sp-texto-secundario">Fato em {formatarDataHora(relato.dataOcorrencia)}</span>
              </span>
            </span>
            <span className="sp-novo-relato-acoes">
              {relato.dataRegistro && <span className="sp-texto-secundario">Registrado em {formatarDataHora(relato.dataRegistro)}</span>}
              <Button variant="secondary" onClick={() => aoAbrir(relato.idOcorrencia)}
                aria-label={`Abrir novo relato ${relato.idOcorrencia} para triagem`}>Abrir</Button>
            </span>
          </li>
        })}
      </ul>
      {relatos.length > LIMITE && <p className="sp-texto-secundario">
        E mais {relatos.length - LIMITE}. Filtre a lista pela situação Aberta para ver todos.
      </p>}
    </div>
  </section>
}
