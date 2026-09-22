// Tabela do catálogo de EPIs (UC06, passo 2): nome, localização, saldo, mínimo e situação, com
// botão de detalhes/histórico para todos e botão de movimentação só para o Gestor (RNF03).

import { Button, Table, TableRow } from '../../shared/components'
import type { Epi } from './api'
import { SituacaoEstoque } from './SituacaoEstoque'

export function EpisTabela({ epis, podeMovimentar, aoDetalhar, aoMovimentar }: {
  epis: Epi[]
  podeMovimentar: boolean
  aoDetalhar: (epi: Epi) => void
  aoMovimentar: (epi: Epi) => void
}) {
  return <Table caption="Catálogo de EPIs em estoque">
    <thead><tr>
      <th scope="col">EPI</th><th scope="col">Localização</th><th scope="col">Saldo</th>
      <th scope="col">Mínimo</th><th scope="col">Situação</th><th scope="col">Ações</th>
    </tr></thead>
    <tbody>{epis.map(epi => <TableRow key={epi.id} className={epi.estoqueCritico ? 'sp-linha-critica' : undefined}>
      <td><strong>{epi.nome}</strong><br /><span className="sp-texto-secundario">CA {epi.numeroCa || 'não informado'}</span></td>
      <td>{epi.localizacao ?? 'Não informada'}</td>
      <td>{epi.quantidade}</td>
      <td>{epi.estoqueMinimo}</td>
      <td><SituacaoEstoque epi={epi} /></td>
      <td><div className="sp-acoes-linha">
        <Button variant="secondary" onClick={() => aoDetalhar(epi)} aria-label={`Detalhes e histórico de ${epi.nome}`}>Detalhes</Button>
        {podeMovimentar && <Button variant="primary" icon="plus" onClick={() => aoMovimentar(epi)}
          aria-label={`Movimentar estoque de ${epi.nome}`}>Movimentar</Button>}
      </div></td>
    </TableRow>)}</tbody>
  </Table>
}
