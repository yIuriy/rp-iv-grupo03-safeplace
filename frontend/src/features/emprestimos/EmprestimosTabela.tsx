// Tabela de empréstimos (RF11; US11, critérios 2 e 3): EPI, colaborador em posse, entrega, devolução
// prevista e situação em texto (Em posse, Atrasado ou Devolvido, com a condição informada), com
// detalhes para todos e registro de devolução só para empréstimos ativos.

import { Badge, Button, Table, TableRow } from '../../shared/components'
import { formatarData, formatarDataHora } from '../estoque/formatos'
import type { Emprestimo } from './api'
import { rotuloDoEstado, situacaoDoEmprestimo } from './formatos'

function Situacao({ emprestimo }: { emprestimo: Emprestimo }) {
  const situacao = situacaoDoEmprestimo(emprestimo)
  return <>
    <Badge tone={situacao.tom}>{situacao.rotulo}</Badge>
    {emprestimo.dataDevolucaoReal && <><br /><span className="sp-texto-secundario">
      {formatarDataHora(emprestimo.dataDevolucaoReal)}
      {emprestimo.estadoDevolucao && `, ${rotuloDoEstado[emprestimo.estadoDevolucao].toLowerCase()}`}
    </span></>}
  </>
}

/** Na duplicidade confirmada, o mesmo EPI fica duas vezes com a mesma pessoa; a entrega distingue os botões. */
function descricaoDaLinha(emprestimo: Emprestimo, preposicao: 'para' | 'por'): string {
  return `${emprestimo.epiNome} ${preposicao} ${emprestimo.colaboradorNome}, entregue em ${formatarDataHora(emprestimo.dataRetirada)}`
}

export function EmprestimosTabela({ emprestimos, aoDetalhar, aoDevolver }: {
  emprestimos: Emprestimo[]
  aoDetalhar: (emprestimo: Emprestimo) => void
  aoDevolver: (emprestimo: Emprestimo) => void
}) {
  return <Table caption="Empréstimos de EPIs">
    <thead><tr>
      <th scope="col">EPI</th><th scope="col">Colaborador</th><th scope="col">Entrega</th>
      <th scope="col">Devolução prevista</th><th scope="col">Situação</th><th scope="col">Ações</th>
    </tr></thead>
    <tbody>{emprestimos.map(emprestimo => <TableRow key={emprestimo.id}
      className={emprestimo.atrasado && emprestimo.status !== 'DISPONIVEL' ? 'sp-linha-critica' : undefined}>
      <td><strong>{emprestimo.epiNome}</strong><br /><span className="sp-texto-secundario">Código {emprestimo.epiId}</span></td>
      <td>{emprestimo.colaboradorNome}</td>
      <td>{formatarDataHora(emprestimo.dataRetirada)}</td>
      <td>{formatarData(emprestimo.dataDevolucaoPrevista)}</td>
      <td><Situacao emprestimo={emprestimo} /></td>
      <td><div className="sp-acoes-linha">
        <Button variant="secondary" onClick={() => aoDetalhar(emprestimo)}
          aria-label={`Detalhes do empréstimo de ${descricaoDaLinha(emprestimo, 'para')}`}>Detalhes</Button>
        {emprestimo.status !== 'DISPONIVEL' && <Button variant="primary" onClick={() => aoDevolver(emprestimo)}
          aria-label={`Registrar devolução de ${descricaoDaLinha(emprestimo, 'por')}`}>Registrar devolução</Button>}
      </div></td>
    </TableRow>)}</tbody>
  </Table>
}
