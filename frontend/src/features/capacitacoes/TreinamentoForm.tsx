// Cadastro de treinamento realizado por um ou mais colaboradores. A validade em meses é
// opcional: sem ela o treinamento não vence (#116, #192).

import { useState, type FormEvent } from 'react'
import { Alert, Button, Checkbox, TextField } from '../../shared/components'
import { cadastrarTreinamento, descreverFalhaCadastro } from './api'
import { validarTreinamento, type CampoTreinamento, type Erros, type RascunhoTreinamento } from './validacaoCapacitacao'
import { useColaboradoresOpcoes } from './useColaboradoresOpcoes'

const vazio: RascunhoTreinamento = {
  nome: '', conteudo: '', instrutor: '', dataRealizacao: '', cargaHoraria: '', validadeMeses: '', colaboradoresIds: [],
}

const ordemDeFoco: CampoTreinamento[] = ['nome', 'dataRealizacao', 'cargaHoraria', 'validadeMeses', 'colaboradoresIds']

export function TreinamentoForm({ hoje }: { hoje: string }) {
  const colaboradores = useColaboradoresOpcoes()
  const [rascunho, setRascunho] = useState(vazio)
  const [erros, setErros] = useState<Erros<CampoTreinamento>>({})
  const [falha, setFalha] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [enviando, setEnviando] = useState(false)

  const alterar = (alteracao: Partial<RascunhoTreinamento>) => {
    setRascunho(atual => ({ ...atual, ...alteracao }))
    setSucesso('')
  }

  const alternarColaborador = (id: number, marcado: boolean) => alterar({
    colaboradoresIds: marcado ? [...rascunho.colaboradoresIds, id] : rascunho.colaboradoresIds.filter(atual => atual !== id),
  })

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const { dados, erros: pendencias } = validarTreinamento(rascunho, hoje)
    setErros(pendencias)
    if (!dados) {
      const primeiro = ordemDeFoco.find(campo => pendencias[campo])
      if (primeiro) document.getElementById(`treinamento-${primeiro}`)?.focus()
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      const salvo = await cadastrarTreinamento(dados)
      setSucesso(`Treinamento "${salvo.nome}" cadastrado.`)
      setRascunho(vazio)
    } catch (erro) {
      setFalha(descreverFalhaCadastro(erro))
    } finally {
      setEnviando(false)
    }
  }

  return <section className="sp-secao" aria-label="Cadastrar treinamento">
    <h2 className="sp-type-heading-sm">Cadastrar treinamento</h2>
    <form className="sp-form" noValidate onSubmit={enviar}>
      <TextField id="treinamento-nome" label="Treinamento" value={rascunho.nome} error={erros.nome} autoComplete="off"
        placeholder="Ex.: NR-10 Segurança em Eletricidade" onChange={e => alterar({ nome: e.target.value })} />
      <TextField id="treinamento-conteudo" label="Conteúdo (opcional)" value={rascunho.conteudo} autoComplete="off"
        onChange={e => alterar({ conteudo: e.target.value })} />
      <TextField id="treinamento-instrutor" label="Instrutor (opcional)" value={rascunho.instrutor} autoComplete="off"
        onChange={e => alterar({ instrutor: e.target.value })} />
      <TextField id="treinamento-dataRealizacao" label="Data de realização" type="date" max={hoje} value={rascunho.dataRealizacao}
        error={erros.dataRealizacao} onChange={e => alterar({ dataRealizacao: e.target.value })} />
      <TextField id="treinamento-cargaHoraria" label="Carga horária em horas (opcional)" type="number" min={1}
        value={rascunho.cargaHoraria} error={erros.cargaHoraria} onChange={e => alterar({ cargaHoraria: e.target.value })} />
      <TextField id="treinamento-validadeMeses" label="Validade em meses (opcional)" type="number" min={1}
        value={rascunho.validadeMeses} error={erros.validadeMeses}
        helper="Deixe em branco se o treinamento não vence."
        onChange={e => alterar({ validadeMeses: e.target.value })} />
      <fieldset className="sp-secao" aria-describedby={erros.colaboradoresIds ? 'treinamento-colaboradores-erro' : undefined}>
        <legend className="sp-field-label">Colaboradores que realizaram</legend>
        {colaboradores.erro && <p>Não foi possível carregar os colaboradores.</p>}
        {colaboradores.dados?.map((c, indice) => <Checkbox key={c.colaboradorId}
          id={indice === 0 ? 'treinamento-colaboradoresIds' : undefined} label={c.colaboradorNome}
          checked={rascunho.colaboradoresIds.includes(c.colaboradorId)}
          onChange={e => alternarColaborador(c.colaboradorId, e.target.checked)} />)}
        {erros.colaboradoresIds && <p id="treinamento-colaboradores-erro" role="alert"
          className="sp-field-message sp-field-message--error">{erros.colaboradoresIds}</p>}
      </fieldset>
      {falha && <Alert tone="danger" title="Não foi possível cadastrar o treinamento" live="assertive">{falha}</Alert>}
      {sucesso && <Alert tone="success" title="Cadastro realizado" live="polite">{sucesso}</Alert>}
      <div><Button type="submit" loading={enviando} icon="check">Cadastrar treinamento</Button></div>
    </form>
  </section>
}
