// Diálogo de detalhes de um EPI (UC06, passo 4): dados do item e histórico de movimentações com
// operador, data/hora, tipo, quantidade, motivo e saldo após cada movimento (RNF05).

import { useCallback } from 'react'
import { Alert, Button, Dialog, EmptyState } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarData, formatarDataHora } from '../../shared/formatos'
import type { Epi } from '../epis/epi'
import { listarHistorico } from './api'
import { rotuloDaClassificacao, rotuloDoTipo } from './formatos'
import { SituacaoEstoque } from './SituacaoEstoque'

function Historico({ epiId, versao }: { epiId: number; versao: number }) {
  const carregar = useCallback((signal: AbortSignal) => listarHistorico(epiId, signal), [epiId])
  const consulta = useConsulta(`${epiId}:${versao}`, carregar)

  if (consulta.erro) {
    return <>
      <Alert tone="danger" live="assertive" title="Não foi possível carregar o histórico.">
        Verifique sua conexão ou tente novamente em alguns instantes.
      </Alert>
      <div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div>
    </>
  }
  if (consulta.dados === null) return <p role="status">Carregando histórico...</p>
  if (consulta.dados.length === 0) {
    return <EmptyState title="Sem movimentações registradas" description="Entradas e saídas deste EPI aparecerão aqui com operador, data e hora." />
  }
  return <ul className="sp-historico" aria-label="Histórico de movimentações">
    {consulta.dados.map(mov => <li key={mov.id} className="sp-history-item">
      <div>
        <time dateTime={mov.dataHora}>{formatarDataHora(mov.dataHora)}</time>
        <h3>{rotuloDoTipo[mov.tipo]} de {mov.quantidade} {mov.quantidade === 1 ? 'unidade' : 'unidades'}</h3>
        <p>Operador: {mov.operador}. Motivo: {mov.motivo ?? 'não informado'}.
          {Number.isFinite(mov.saldoAtual) && ` Saldo após: ${mov.saldoAtual}.`}</p>
      </div>
    </li>)}
  </ul>
}

/** `versao` muda quando uma movimentação é registrada, forçando o histórico a recarregar. */
export function EpiDetalheDialog({ epi, versao, aoFechar }: { epi: Epi | null; versao: number; aoFechar: () => void }) {
  return <Dialog open={epi !== null} title={epi?.nome ?? 'EPI'} description="Dados do equipamento e histórico de movimentações." onClose={aoFechar}>
    {epi && <div className="sp-dialog-body">
      <dl className="sp-detalhes">
        <div><dt>Situação</dt><dd><SituacaoEstoque epi={epi} /></dd></div>
        <div><dt>Saldo</dt><dd>{epi.quantidade} (mínimo {epi.estoqueMinimo})</dd></div>
        <div><dt>Localização</dt><dd>{epi.localizacao ?? 'Não informada'}</dd></div>
        <div><dt>Classificação</dt><dd>{rotuloDaClassificacao(epi.classificacao)}</dd></div>
        <div><dt>CA</dt><dd>{epi.numeroCa || 'Não informado'}{epi.dataValidadeCa && `, válido até ${formatarData(epi.dataValidadeCa)}`}</dd></div>
        {epi.descricao && <div><dt>Descrição</dt><dd>{epi.descricao}</dd></div>}
      </dl>
      <h3 className="sp-type-heading-sm">Histórico de movimentações</h3>
      <Historico epiId={epi.id} versao={versao} />
      <div className="sp-dialog-actions"><Button variant="secondary" onClick={aoFechar}>Fechar</Button></div>
    </div>}
  </Dialog>
}
