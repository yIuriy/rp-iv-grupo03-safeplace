// Janela com o histórico de uma tarefa para o Gestor de Segurança (UC11, regra 1; issue #114):
// cadastro e cada classificação, com nível anterior e novo, data, hora e responsável técnico.

import { useCallback } from 'react'
import { Button, Dialog } from '../../shared/components'
import { HistoricoAlteracoes } from '../areas-risco/HistoricoAlteracoes'
import { listarHistoricoTarefa, type Tarefa } from './api'

export function HistoricoTarefaDialog({ tarefa, aoFechar }: {
  tarefa: Tarefa | null
  aoFechar: () => void
}) {
  const id = tarefa?.id
  const carregar = useCallback(
    (signal: AbortSignal) => listarHistoricoTarefa(id ?? 0, signal),
    [id]
  )

  return (
    <Dialog
      open={tarefa !== null}
      title={tarefa ? `Histórico: ${tarefa.descricao}` : 'Histórico da tarefa'}
      description="Registros de cadastro e de classificação de periculosidade, do mais recente ao mais antigo."
      onClose={aoFechar}
    >
      <div className="sp-dialog-body">
        {tarefa && (
          <HistoricoAlteracoes
            titulo="Registros da tarefa"
            chave={`historico-tarefa-${tarefa.id}`}
            carregar={carregar}
          />
        )}
        <div className="sp-dialog-actions">
          <Button variant="secondary" onClick={aoFechar}>
            Fechar histórico
          </Button>
        </div>
      </div>
    </Dialog>
  )
}
