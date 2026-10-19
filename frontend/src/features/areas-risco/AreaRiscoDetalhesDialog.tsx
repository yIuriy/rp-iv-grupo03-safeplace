// Consulta de uma área de risco: setor físico, agentes, limites e EPIs obrigatórios para acesso
//. O Gestor pode seguir para a edição e abrir o histórico de alterações (#114); o
// Supervisor apenas consulta.

import { Alert, Badge, Button, Dialog } from '../../shared/components'
import { descreverSetor, rotuloNivelPerigo, tomDoNivelPerigo, type AreaRisco, type Setor } from './api'
import { HistoricoAreaRisco } from './HistoricoAlteracoes'

export function AreaRiscoDetalhesDialog({
  area,
  setores,
  podeEditar,
  aoFechar,
  aoEditar,
}: {
  area: AreaRisco | null
  setores: Setor[]
  podeEditar: boolean
  aoFechar: () => void
  aoEditar?: (area: AreaRisco) => void
}) {
  if (!area) return null

  const setorEncontrado = area.setorId !== null ? setores.find(s => s.id === area.setorId) : null
  const localizacao = setorEncontrado
    ? descreverSetor(setorEncontrado)
    : area.limites || 'Não informado'

  return (
    <Dialog
      open={!!area}
      title={`${area.codigo} - ${area.nome}`}
      description="Consulta das diretrizes de segurança, perigos mapeados e proteção obrigatória para acesso à área."
      onClose={aoFechar}
    >
      <div className="sp-dialog-body">
        <dl className="sp-risk-facts ds-dialog-content">
          <div>
            <dt>Grau de perigo</dt>
            <dd>
              <Badge tone={tomDoNivelPerigo[area.nivelPerigo]}>
                {rotuloNivelPerigo[area.nivelPerigo]}
              </Badge>
            </dd>
          </div>
          <div>
            <dt>Localização / Setor</dt>
            <dd>{localizacao}</dd>
          </div>
          {area.limites && (
            <div>
              <dt>Limites demarcados</dt>
              <dd>{area.limites}</dd>
            </div>
          )}
          {area.descricao && (
            <div>
              <dt>Descrição</dt>
              <dd>{area.descricao}</dd>
            </div>
          )}
          <div>
            <dt>Agentes de risco identificados</dt>
            <dd>
              {area.agentesRisco.length > 0
                ? area.agentesRisco.join(', ')
                : 'Nenhum agente específico cadastrado'}
            </dd>
          </div>
          <div>
            <dt>Vínculos de proteção para acesso à área</dt>
            <dd>
              {area.episObrigatorios.length > 0 ? (
                <ul className="sp-lista-epis-vinculados">
                  {area.episObrigatorios.map(epi => (
                    <li key={epi.id}>
                      <strong>{epi.nome}</strong> (CA {epi.numeroCa})
                    </li>
                  ))}
                </ul>
              ) : (
                <p>Nenhum EPI obrigatório cadastrado.</p>
              )}
            </dd>
          </div>
        </dl>

        <Alert tone="info" title="Requisito de proteção para ingresso na área">
          Estes EPIs são exigidos para qualquer pessoa que acesse ou circule neste setor físico.
          Não se trata do recálculo automático de vínculos de proteção por função.
        </Alert>

        {podeEditar && <HistoricoAreaRisco key={area.id} areaId={area.id} />}

        <div className="sp-dialog-actions">
          <Button variant="secondary" onClick={aoFechar}>
            Fechar consulta
          </Button>
          {podeEditar && aoEditar && (
            <Button
              variant="primary"
              onClick={() => {
                aoEditar(area)
              }}
            >
              Editar área
            </Button>
          )}
        </div>
      </div>
    </Dialog>
  )
}
