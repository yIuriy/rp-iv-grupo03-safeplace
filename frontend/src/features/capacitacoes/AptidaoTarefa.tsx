// Consulta de aptidão para a tarefa pretendida e tentativa de alocação (US10, critérios 3 e 4; UC02,
// exceção I e cenário alternativo II; UC11, exceção I). A consulta vem da mesma regra da confirmação
// de alocação (#117) e é só informativa: a alocação é sempre validada de novo pelo servidor, que
// recusa com os motivos quando há capacitação vencida ou pendente (#192).

import { useRef, useState, type FormEvent } from 'react'
import { Alert, Button, Select } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { listarTarefas, rotuloNivelPerigo, type Tarefa } from '../tarefas/api'
import { alocarColaborador, consultarAptidao, descreverFalha, type Aptidao, type ResultadoAlocacao } from './api'

type Estado =
  | { tipo: 'inicial' }
  | { tipo: 'consultando' }
  | { tipo: 'resultado'; aptidao: Aptidao }
  | { tipo: 'falha'; mensagem: string }

type EstadoAlocacao =
  | { tipo: 'inicial' }
  | { tipo: 'alocando' }
  | { tipo: 'resultado'; resultado: ResultadoAlocacao }
  | { tipo: 'falha' }

function rotuloDaTarefa(tarefa: Tarefa): string {
  return tarefa.nivelPerigo ? `${tarefa.descricao} (${rotuloNivelPerigo[tarefa.nivelPerigo]})` : `${tarefa.descricao} (sem classificação)`
}

export function AptidaoTarefa({ colaboradorId, colaboradorNome }: { colaboradorId: number; colaboradorNome: string }) {
  const tarefas = useConsulta('capacitacoes-tarefas', listarTarefas)
  const [tarefaId, setTarefaId] = useState('')
  const [erroCampo, setErroCampo] = useState('')
  const [estado, setEstado] = useState<Estado>({ tipo: 'inicial' })
  const [alocacao, setAlocacao] = useState<EstadoAlocacao>({ tipo: 'inicial' })
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
    setAlocacao({ tipo: 'inicial' })
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

  async function alocar() {
    if (alocacao.tipo === 'alocando') return
    setAlocacao({ tipo: 'alocando' })
    try {
      setAlocacao({ tipo: 'resultado', resultado: await alocarColaborador(Number(tarefaId), colaboradorId) })
    } catch {
      setAlocacao({ tipo: 'falha' })
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
            setAlocacao({ tipo: 'inicial' })
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
    {estado.tipo === 'resultado' && <div>
      <Button onClick={alocar} loading={alocacao.tipo === 'alocando'}>
        Alocar {colaboradorNome} em {estado.aptidao.tarefaDescricao}
      </Button>
    </div>}
    <div aria-live="polite">
      {alocacao.tipo === 'falha' && <Alert tone="danger" title="Não foi possível alocar.">
        O servidor não confirmou a alocação. Verifique a conexão e tente novamente.
      </Alert>}
      {alocacao.tipo === 'resultado' && <ResultadoDaAlocacao resultado={alocacao.resultado} colaboradorNome={colaboradorNome} />}
    </div>
    <p className="sp-texto-secundario">
      A consulta é informativa. A alocação só é confirmada pelo servidor, que valida novamente as exigências no momento da confirmação.
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

function ResultadoDaAlocacao({ resultado, colaboradorNome }: { resultado: ResultadoAlocacao; colaboradorNome: string }) {
  if (resultado.alocada) return <Alert tone="success" title="Alocação confirmada">{colaboradorNome} foi alocado na tarefa.</Alert>
  return <Alert tone="danger" title={resultado.titulo}>
    <p>{resultado.mensagem}</p>
    {resultado.impedimentos.length > 0 && <ul className="sp-pendencias">
      {resultado.impedimentos.map(motivo => <li key={motivo}>{motivo}</li>)}
    </ul>}
  </Alert>
}
