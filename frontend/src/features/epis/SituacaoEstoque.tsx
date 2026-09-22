// Indicador da situação do estoque de um EPI: exibe a situação cadastral e, quando o backend
// marca estoque crítico (saldo <= mínimo, US03 regra 3), um selo com ícone e texto "Crítico".
// O aviso não depende só da cor (RNF11, WCAG 2.1 AA; issue #123).

import { Badge, Icon, type FeedbackTone } from '../../shared/components'
import type { Epi } from './api'
import { rotuloDoStatus } from './formatos'

const tomDoStatus: Record<Epi['status'], FeedbackTone> = {
  DISPONIVEL: 'success',
  EM_USO: 'info',
  EM_MANUTENCAO: 'warning',
  ESGOTADO: 'danger',
  DESCARTADO: 'info',
}

export function SituacaoEstoque({ epi }: { epi: Epi }) {
  return <span className="sp-situacao-estoque">
    <Badge tone={tomDoStatus[epi.status]}>{rotuloDoStatus[epi.status]}</Badge>
    {epi.estoqueCritico && <Badge tone="danger">
      <Icon name="alert-triangle" className="sp-situacao-icone" />
      <span>Crítico</span>
      <span className="sp-sr-only">{`: saldo ${epi.quantidade} igual ou abaixo do mínimo ${epi.estoqueMinimo}`}</span>
    </Badge>}
  </span>
}
