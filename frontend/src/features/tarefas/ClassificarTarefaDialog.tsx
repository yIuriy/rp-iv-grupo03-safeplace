// Diálogo de classificação do grau de perigo de uma tarefa pelo Gestor (UC11). O fluxo de envio vem do
// hook useEnvioFormulario (Template Method, #243).

import { useState } from 'react'
import { Alert, Button, Dialog, Select } from '../../shared/components'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'
import {
  classificarTarefa,
  niveisPerigo,
  rotuloNivelPerigo,
  type NivelPerigo,
  type Tarefa,
} from './api'

export function ClassificarTarefaDialog({
  tarefa,
  aoFechar,
  aoSalvar,
}: {
  tarefa: Tarefa | null
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  if (!tarefa) return null
  return <DialogoClassificacao key={tarefa.id} tarefa={tarefa} aoFechar={aoFechar} aoSalvar={aoSalvar} />
}

function DialogoClassificacao({
  tarefa,
  aoFechar,
  aoSalvar,
}: {
  tarefa: Tarefa
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  const [nivelPerigo, setNivelPerigo] = useState<NivelPerigo>(tarefa.nivelPerigo ?? 'MEDIO')

  const { enviando, falha: erro, onSubmit } = useEnvioFormulario({
    ler: () => nivelPerigo,
    validar: nivel => ({ dados: nivel, erros: {} }),
    ordem: [],
    enviar: async nivel => {
      aoSalvar(await classificarTarefa(tarefa.id, { nivelPerigo: nivel }))
      aoFechar()
    },
    descreverFalha: criarDescritorDeFalha({},
      usarMensagemDaApi('Não foi possível atualizar a classificação da tarefa. Tente novamente.')),
    liberarAposSucesso: true,
  }, {})

  return (
    <Dialog
      open
      title={tarefa.classificada ? 'Reavaliar periculosidade da tarefa' : 'Classificar tarefa'}
      description="Atribua o grau de periculosidade da atividade operacional conforme a avaliação técnica de segurança."
      onClose={aoFechar}
    >
      <form onSubmit={onSubmit} className="sp-dialog-body" noValidate>
        {erro && (
          <Alert tone="danger" title="Não foi possível classificar" live="assertive">
            {erro}
          </Alert>
        )}

        <div className="sp-campo-somente-leitura">
          <p className="sp-field-label">Descrição da atividade</p>
          <p className="sp-descricao-tarefa">{tarefa.descricao}</p>
        </div>

        <Select
          label="Grau de periculosidade *"
          value={nivelPerigo}
          onChange={e => setNivelPerigo(e.target.value as NivelPerigo)}
          helper="Mesma escala das áreas de risco: Baixo, Médio, Alto ou Crítico. Você ficará registrado como responsável técnico."
          required
        >
          {niveisPerigo.map(nivel => (
            <option key={nivel} value={nivel}>
              {rotuloNivelPerigo[nivel]}
            </option>
          ))}
        </Select>

        <div className="sp-dialog-actions">
          <Button type="button" variant="secondary" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary" disabled={enviando}>
            {enviando ? 'Gravando...' : 'Salvar classificação'}
          </Button>
        </div>
      </form>
    </Dialog>
  )
}
