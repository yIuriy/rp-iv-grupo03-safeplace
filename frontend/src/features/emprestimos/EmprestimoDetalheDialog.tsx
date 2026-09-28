// Diálogo de detalhes de um empréstimo (UC12, regra 2; US11, critérios 1 e 2; RNF05): situação,
// titular, item, datas, responsáveis pela entrega e pela devolução, condição informada, justificativa
// de substituição e o termo de cautela registrado na entrega.

import { useCallback } from 'react'
import { Badge, Button, Dialog } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarData, formatarDataHora } from '../estoque/formatos'
import { nomesDosResponsaveis, type Emprestimo } from './api'
import { rotuloDoEstado, situacaoDoEmprestimo } from './formatos'

export function EmprestimoDetalheDialog({ emprestimo, aoFechar }: { emprestimo: Emprestimo | null; aoFechar: () => void }) {
  return <Dialog open={emprestimo !== null} title="Detalhes do empréstimo" onClose={aoFechar}
    description={emprestimo ? `${emprestimo.epiNome} com ${emprestimo.colaboradorNome}.` : undefined}>
    {emprestimo && <Detalhes key={emprestimo.id} emprestimo={emprestimo} aoFechar={aoFechar} />}
  </Dialog>
}

function Detalhes({ emprestimo, aoFechar }: { emprestimo: Emprestimo; aoFechar: () => void }) {
  const carregar = useCallback(() => nomesDosResponsaveis(emprestimo), [emprestimo])
  const responsaveis = useConsulta(`responsaveis-${emprestimo.id}`, carregar)
  const situacao = situacaoDoEmprestimo(emprestimo)
  const nome = (valor: string | null | undefined) => responsaveis.carregando ? 'carregando...' : valor ?? 'Não identificado'

  return <div className="sp-dialog-body">
    <dl className="sp-detalhes">
      <div><dt>Situação</dt><dd><Badge tone={situacao.tom}>{situacao.rotulo}</Badge></dd></div>
      <div><dt>Colaborador</dt><dd>{emprestimo.colaboradorNome}</dd></div>
      <div><dt>EPI</dt><dd>{emprestimo.epiNome} (código {emprestimo.epiId})</dd></div>
      <div><dt>Entrega</dt><dd>{formatarDataHora(emprestimo.dataRetirada)}, por {nome(responsaveis.dados?.entrega)}</dd></div>
      <div><dt>Devolução prevista</dt><dd>{formatarData(emprestimo.dataDevolucaoPrevista)}</dd></div>
      {emprestimo.dataDevolucaoReal && <div><dt>Devolução</dt>
        <dd>{formatarDataHora(emprestimo.dataDevolucaoReal)}, por {nome(responsaveis.dados?.devolucao)}</dd></div>}
      {emprestimo.estadoDevolucao && <div><dt>Condição na devolução</dt><dd>{rotuloDoEstado[emprestimo.estadoDevolucao]}</dd></div>}
      {emprestimo.justificativaSubstituicao && <div><dt>Justificativa de substituição</dt>
        <dd>{emprestimo.justificativaSubstituicao}</dd></div>}
    </dl>
    <div className="sp-secao">
      <h3 className="sp-type-heading-sm">Termo de cautela</h3>
      <p className="sp-termo">{emprestimo.termoCautela || 'Termo não informado pelo servidor.'}</p>
    </div>
    <div className="sp-dialog-actions"><Button variant="secondary" onClick={aoFechar}>Fechar</Button></div>
  </div>
}
