import { useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Select } from '../../shared/components'
import { mensagemDaApi } from '../../shared/api/erros'
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
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function submeter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro(null)
    setEnviando(true)

    try {
      const atualizada = await classificarTarefa(tarefa.id, { nivelPerigo })
      aoSalvar(atualizada)
      aoFechar()
    } catch (err: unknown) {
      setErro(
        mensagemDaApi(err) ||
          'Não foi possível atualizar a classificação da tarefa. Tente novamente.'
      )
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog
      open
      title={tarefa.classificada ? 'Reavaliar periculosidade da tarefa' : 'Classificar tarefa'}
      description="Atribua o grau de periculosidade da atividade operacional conforme a avaliação técnica de segurança."
      onClose={aoFechar}
    >
      <form onSubmit={submeter} className="sp-dialog-body" noValidate>
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
