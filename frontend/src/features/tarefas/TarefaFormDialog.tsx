import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Select, TextArea } from '../../shared/components'
import { mensagemDaApi } from '../../shared/api/erros'
import {
  cadastrarTarefa,
  niveisPerigo,
  rotuloNivelPerigo,
  type NivelPerigo,
  type Tarefa,
} from './api'

export function TarefaFormDialog({
  open,
  aoFechar,
  aoSalvar,
}: {
  open: boolean
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  const [descricao, setDescricao] = useState('')
  const [nivelPerigo, setNivelPerigo] = useState<string>('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      setDescricao('')
      setNivelPerigo('')
      setErro(null)
      setEnviando(false)
    }
  }, [open])

  async function submeter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro(null)

    if (!descricao.trim()) {
      setErro('A descrição da atividade é obrigatória.')
      return
    }

    setEnviando(true)
    try {
      const tarefa = await cadastrarTarefa({
        descricao: descricao.trim(),
        nivelPerigo: nivelPerigo ? (nivelPerigo as NivelPerigo) : undefined,
      })
      aoSalvar(tarefa)
      aoFechar()
    } catch (err: unknown) {
      setErro(mensagemDaApi(err) || 'Não foi possível cadastrar a tarefa. Verifique os dados informados.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog
      open={open}
      title="Cadastrar nova tarefa"
      description="Informe a descrição da atividade operacional. A classificação de periculosidade pode ser definida agora ou atribuída posteriormente pelo Gestor de Segurança (UC11)."
      onClose={aoFechar}
    >
      <form onSubmit={submeter} className="sp-dialog-body" noValidate>
        {erro && (
          <Alert tone="danger" title="Não foi possível salvar" live="assertive">
            {erro}
          </Alert>
        )}

        <TextArea
          label="Descrição da atividade operacional *"
          value={descricao}
          onChange={e => setDescricao(e.target.value)}
          placeholder="Ex: Solda em altura na estrutura metálica do galpão de estoque..."
          rows={4}
          required
        />

        <Select
          label="Grau de periculosidade inicial (opcional)"
          value={nivelPerigo}
          onChange={e => setNivelPerigo(e.target.value)}
          helper="Selecione caso a atividade já tenha sido classificada tecnicamente."
        >
          <option value="">Não classificada no momento</option>
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
            {enviando ? 'Cadastrando...' : 'Cadastrar tarefa'}
          </Button>
        </div>
      </form>
    </Dialog>
  )
}
