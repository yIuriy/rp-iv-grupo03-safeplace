// Definição da certificação ou do treinamento exigido para uma tarefa ou EPI (UC02, exceção I).

import { useState, type FormEvent } from 'react'
import { Alert, Button, Select, TextField } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { listarEpis } from '../epis/api'
import { listarTarefas } from '../tarefas/api'
import { definirExigencia, descreverFalhaCadastro, type AlvoExigencia } from './api'
import { validarExigencia, type CampoExigencia, type Erros, type RascunhoExigencia } from './validacaoCapacitacao'

const vazio: RascunhoExigencia = { nomeCapacitacao: '', tipoCapacitacao: '', alvo: '', alvoId: '' }

const ordemDeFoco: CampoExigencia[] = ['nomeCapacitacao', 'tipoCapacitacao', 'alvo', 'alvoId']

export function ExigenciaForm() {
  const tarefas = useConsulta('capacitacoes-cadastro-tarefas', listarTarefas)
  const epis = useConsulta('capacitacoes-cadastro-epis', listarEpis)
  const [rascunho, setRascunho] = useState(vazio)
  const [erros, setErros] = useState<Erros<CampoExigencia>>({})
  const [falha, setFalha] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [enviando, setEnviando] = useState(false)

  const alterar = (alteracao: Partial<RascunhoExigencia>) => {
    setRascunho(atual => ({ ...atual, ...alteracao }))
    setSucesso('')
  }

  const opcoesDoAlvo: { id: number; rotulo: string }[] = rascunho.alvo === 'TAREFA'
    ? (tarefas.dados ?? []).map(t => ({ id: t.id, rotulo: t.descricao }))
    : rascunho.alvo === 'EPI' ? (epis.dados ?? []).map(e => ({ id: e.id, rotulo: `${e.nome} (CA ${e.numeroCa})` })) : []

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const { dados, erros: pendencias } = validarExigencia(rascunho)
    setErros(pendencias)
    if (!dados) {
      const primeiro = ordemDeFoco.find(campo => pendencias[campo])
      if (primeiro) document.getElementById(`exigencia-${primeiro}`)?.focus()
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      await definirExigencia(dados)
      setSucesso(`Exigência "${dados.nomeCapacitacao}" definida.`)
      setRascunho(vazio)
    } catch (erro) {
      setFalha(descreverFalhaCadastro(erro))
    } finally {
      setEnviando(false)
    }
  }

  return <section className="sp-secao" aria-label="Definir exigência">
    <h2 className="sp-type-heading-sm">Definir exigência de capacitação</h2>
    <form className="sp-form" noValidate onSubmit={enviar}>
      <TextField id="exigencia-nomeCapacitacao" label="Certificação ou treinamento exigido" value={rascunho.nomeCapacitacao}
        error={erros.nomeCapacitacao} autoComplete="off" helper="Use o mesmo nome do cadastro da certificação ou do treinamento."
        onChange={e => alterar({ nomeCapacitacao: e.target.value })} />
      <Select id="exigencia-tipoCapacitacao" label="Tipo" value={rascunho.tipoCapacitacao} error={erros.tipoCapacitacao}
        onChange={e => alterar({ tipoCapacitacao: e.target.value as RascunhoExigencia['tipoCapacitacao'] })}>
        <option value="">Selecione</option>
        <option value="CERTIFICACAO">Certificação</option>
        <option value="TREINAMENTO">Treinamento</option>
      </Select>
      <Select id="exigencia-alvo" label="Exigida para" value={rascunho.alvo} error={erros.alvo}
        onChange={e => alterar({ alvo: e.target.value as AlvoExigencia | '', alvoId: '' })}>
        <option value="">Selecione</option>
        <option value="TAREFA">Tarefa</option>
        <option value="EPI">EPI</option>
      </Select>
      {rascunho.alvo && <Select id="exigencia-alvoId" label={rascunho.alvo === 'TAREFA' ? 'Tarefa' : 'EPI'} value={rascunho.alvoId}
        error={erros.alvoId} onChange={e => alterar({ alvoId: e.target.value })}>
        <option value="">Selecione</option>
        {opcoesDoAlvo.map(opcao => <option key={opcao.id} value={opcao.id}>{opcao.rotulo}</option>)}
      </Select>}
      {falha && <Alert tone="danger" title="Não foi possível definir a exigência" live="assertive">{falha}</Alert>}
      {sucesso && <Alert tone="success" title="Cadastro realizado" live="polite">{sucesso}</Alert>}
      <div><Button type="submit" loading={enviando} icon="check">Definir exigência</Button></div>
    </form>
  </section>
}
