// Consulta de aptidão para a tarefa pretendida (US10, dicionário e critérios 3 e 4; UC02, exceção I
// e cenário alternativo II). O resultado vem da mesma regra da confirmação de alocação (#117) e é só
// informativo: a tela não oferece alocação, e a confirmação continua validada pelo servidor (#126).

import { useRef, useState, type FormEvent } from 'react'
import { Alert, Button, Select } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { listarTarefas, rotuloNivelPerigo, type Tarefa } from '../tarefas/api'
import { consultarAptidao, descreverFalha, type Aptidao } from './api'

type Estado =
  | { tipo: 'inicial' }
  | { tipo: 'consultando' }
  | { tipo: 'resultado'; aptidao: Aptidao }
  | { tipo: 'falha'; mensagem: string }

function rotuloDaTarefa(tarefa: Tarefa): string {
  return tarefa.nivelPerigo ? `${tarefa.descricao} (${rotuloNivelPerigo[tarefa.nivelPerigo]})` : `${tarefa.descricao} (sem classificação)`
}

export function AptidaoTarefa({ colaboradorId, colaboradorNome }: { colaboradorId: number; colaboradorNome: string }) {
  const tarefas = useConsulta('capacitacoes-tarefas', listarTarefas)
  const [tarefaId, setTarefaId] = useState('')
  const [erroCampo, setErroCampo] = useState('')
  const [estado, setEstado] = useState<Estado>({ tipo: 'inicial' })
  const requisicao = useRef(0)
  const campo = useRef<HTMLSelectElement>(null)

  async function verificar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!tarefaId) {
      setErroCampo('Selecione a tarefa pretendida.')
      campo.current?.focus()
      return
    }
    setErroCampo('')
    const atual = ++requisicao.current
    setEstado({ tipo: 'consultando' })
    try {
      const aptidao = await consultarAptidao(colaboradorId, Number(tarefaId))
      if (atual === requisicao.current) setEstado({ tipo: 'resultado', aptidao })
    } catch (erro) {
      if (atual === requisicao.current) {
        setEstado({ tipo: 'falha', mensagem: descreverFalha(erro, 'Tarefa ou colaborador não encontrado. Recarregue a lista de tarefas.') })
      }
    }
  }

  return <section className="sp-secao sp-aptidao" aria-label="Aptidão para tarefa">
    <div>
      <h3 className="sp-type-heading-sm">Aptidão para tarefa</h3>
      <p className="sp-texto-secundario">
        Verifique se {colaboradorNome} atende às certificações e aos treinamentos exigidos pela tarefa pretendida.
      </p>
    </div>
    {tarefas.erro
      ? <Alert tone="danger" title="Não foi possível carregar as tarefas.">
        <Button variant="secondary" onClick={tarefas.recarregar}>Tentar novamente</Button>
      </Alert>
      : <form className="sp-aptidao-form" noValidate onSubmit={verificar}>
        <Select ref={campo} label="Tarefa pretendida" name="tarefaId" value={tarefaId} error={erroCampo || undefined}
          disabled={tarefas.dados === null}
          helper={tarefas.dados === null ? 'Carregando tarefas...' : tarefas.dados.length === 0 ? 'Nenhuma tarefa cadastrada.' : undefined}
          onChange={event => {
            setTarefaId(event.target.value)
            setErroCampo('')
            requisicao.current++
            setEstado({ tipo: 'inicial' })
          }}>
          <option value="">Selecione</option>
          {tarefas.dados?.map(tarefa => <option key={tarefa.id} value={tarefa.id}>{rotuloDaTarefa(tarefa)}</option>)}
        </Select>
        <Button type="submit" variant="secondary" loading={estado.tipo === 'consultando'}
          disabled={tarefas.dados === null || tarefas.dados.length === 0}>Verificar aptidão</Button>
      </form>}
    <div aria-live="polite">
      {estado.tipo === 'falha' && <Alert tone="danger" title="Não foi possível verificar a aptidão.">{estado.mensagem}</Alert>}
      {estado.tipo === 'resultado' && <Resultado aptidao={estado.aptidao} />}
    </div>
    <p className="sp-texto-secundario">
      Consulta informativa. A alocação só é confirmada pelo servidor, que valida novamente as exigências no momento da confirmação.
    </p>
  </section>
}

function Resultado({ aptidao }: { aptidao: Aptidao }) {
  if (aptidao.apto) {
    return <Alert tone="success" title={`Sem impedimentos para "${aptidao.tarefaDescricao}"`}>
      {aptidao.colaboradorNome} atende às exigências de capacitação registradas para a tarefa na data de hoje.
    </Alert>
  }
  return <Alert tone="danger" title={`Alocação impedida em "${aptidao.tarefaDescricao}"`}>
    <p>Motivos informados pelo servidor:</p>
    <ul className="sp-pendencias">{aptidao.impedimentos.map(motivo => <li key={motivo}>{motivo}</li>)}</ul>
  </Alert>
}
