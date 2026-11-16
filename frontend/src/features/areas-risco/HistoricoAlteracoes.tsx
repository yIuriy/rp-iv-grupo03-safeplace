// Histórico de alterações consultado pelo Gestor de Segurança (UC03, cenário alternativo II; UC11,
// regra 1; issue #114): lista do registro mais recente ao mais antigo, com data, operador e o que
// mudou. Serve a áreas de risco e tarefas, e a área o abre sob demanda dentro do detalhe.

import { useCallback, useId, useState } from 'react'
import { Button, HistoryItem } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarDataHora } from '../../shared/formatos'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import { listarHistoricoAreaRisco, type RegistroHistorico } from './api'
import './historico.css'

const rotuloOperacao: Record<string, string> = {
  CADASTRO: 'Cadastro',
  ALTERACAO: 'Alteração',
  REMOCAO: 'Remoção',
}

export function HistoricoAlteracoes({ titulo, chave, carregar }: {
  titulo: string
  chave: string
  /** Precisa ser estável (`useCallback`), como exige `useConsulta`. */
  carregar: (signal: AbortSignal) => Promise<RegistroHistorico[]>
}) {
  const idTitulo = useId()
  const consulta = useConsulta(chave, carregar)

  return (
    <section className="sp-historico" aria-labelledby={idTitulo}>
      <h3 id={idTitulo} className="sp-type-heading-sm">{titulo}</h3>
      <ResultadoConsulta
        consulta={consulta}
        carregandoLabel="Carregando histórico..."
        erroTitulo="Não foi possível carregar o histórico."
        vazio={{
          title: 'Nenhuma alteração registrada',
          description: 'As alterações passam a aparecer aqui a partir da próxima atualização.',
        }}
      >
        {registros => (
          <ol className="sp-historico-lista">
            {registros.map(registro => (
              <HistoryItem
                key={registro.id}
                dateTime={registro.dataHora}
                dateLabel={formatarDataHora(registro.dataHora, ' às ')}
                title={`${rotuloOperacao[registro.tipoOperacao] ?? registro.tipoOperacao} por ${registro.responsavel}`}
              >
                {registro.descricao ?? 'Sem detalhes registrados.'}
              </HistoryItem>
            ))}
          </ol>
        )}
      </ResultadoConsulta>
    </section>
  )
}

/** Botão do detalhe da área que mostra ou oculta o histórico; só consulta a API quando aberto. */
export function HistoricoAreaRisco({ areaId }: { areaId: number }) {
  const [aberto, setAberto] = useState(false)
  const idRegiao = useId()
  const carregar = useCallback(
    (signal: AbortSignal) => listarHistoricoAreaRisco(areaId, signal),
    [areaId]
  )

  return (
    <div className="sp-historico-area">
      <Button
        variant="secondary"
        icon="clipboard"
        aria-expanded={aberto}
        aria-controls={idRegiao}
        onClick={() => setAberto(valor => !valor)}
      >
        {aberto ? 'Ocultar histórico de alterações' : 'Ver histórico de alterações'}
      </Button>
      <div id={idRegiao} hidden={!aberto}>
        {aberto && (
          <HistoricoAlteracoes
            titulo="Histórico de alterações da área"
            chave={`historico-area-${areaId}`}
            carregar={carregar}
          />
        )}
      </div>
    </div>
  )
}
