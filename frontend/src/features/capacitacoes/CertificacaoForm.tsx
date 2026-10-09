// Cadastro de certificação de um colaborador (UC02): nome, emissão e vencimento.

import { useState, type FormEvent } from 'react'
import { Alert, Button, Select, TextField } from '../../shared/components'
import { cadastrarCertificacao, descreverFalhaCadastro } from './api'
import { validarCertificacao, type CampoCertificacao, type Erros, type RascunhoCertificacao } from './validacaoCapacitacao'
import { useColaboradoresOpcoes } from './useColaboradoresOpcoes'

const vazio: RascunhoCertificacao = { colaboradorId: '', nome: '', dataEmissao: '', dataVencimento: '' }

const ordemDeFoco: CampoCertificacao[] = ['colaboradorId', 'nome', 'dataEmissao', 'dataVencimento']

export function CertificacaoForm({ hoje }: { hoje: string }) {
  const colaboradores = useColaboradoresOpcoes()
  const [rascunho, setRascunho] = useState(vazio)
  const [erros, setErros] = useState<Erros<CampoCertificacao>>({})
  const [falha, setFalha] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [enviando, setEnviando] = useState(false)

  const alterar = (campo: CampoCertificacao, valor: string) => {
    setRascunho(atual => ({ ...atual, [campo]: valor }))
    setSucesso('')
  }

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const { dados, erros: pendencias } = validarCertificacao(rascunho, hoje)
    setErros(pendencias)
    if (!dados) {
      const primeiro = ordemDeFoco.find(campo => pendencias[campo])
      if (primeiro) document.getElementById(`certificacao-${primeiro}`)?.focus()
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      const salva = await cadastrarCertificacao(dados)
      setSucesso(`Certificação "${salva.nome}" cadastrada.`)
      setRascunho(vazio)
    } catch (erro) {
      setFalha(descreverFalhaCadastro(erro))
    } finally {
      setEnviando(false)
    }
  }

  return <section className="sp-secao" aria-label="Cadastrar certificação">
    <h2 className="sp-type-heading-sm">Cadastrar certificação</h2>
    <form className="sp-form" noValidate onSubmit={enviar}>
      <Select id="certificacao-colaboradorId" label="Colaborador" value={rascunho.colaboradorId} error={erros.colaboradorId}
        disabled={colaboradores.dados === null} onChange={e => alterar('colaboradorId', e.target.value)}
        helper={colaboradores.erro ? 'Não foi possível carregar os colaboradores.' : undefined}>
        <option value="">Selecione</option>
        {colaboradores.dados?.map(c => <option key={c.colaboradorId} value={c.colaboradorId}>{c.colaboradorNome}</option>)}
      </Select>
      <TextField id="certificacao-nome" label="Certificação" value={rascunho.nome} error={erros.nome} autoComplete="off"
        placeholder="Ex.: NR-35 Trabalho em Altura" onChange={e => alterar('nome', e.target.value)} />
      <TextField id="certificacao-dataEmissao" label="Data de emissão" type="date" max={hoje} value={rascunho.dataEmissao}
        error={erros.dataEmissao} onChange={e => alterar('dataEmissao', e.target.value)} />
      <TextField id="certificacao-dataVencimento" label="Data de vencimento" type="date" value={rascunho.dataVencimento}
        error={erros.dataVencimento} helper="Deve ser posterior a hoje e à emissão."
        onChange={e => alterar('dataVencimento', e.target.value)} />
      {falha && <Alert tone="danger" title="Não foi possível cadastrar a certificação" live="assertive">{falha}</Alert>}
      {sucesso && <Alert tone="success" title="Cadastro realizado" live="polite">{sucesso}</Alert>}
      <div><Button type="submit" loading={enviando} icon="check">Cadastrar certificação</Button></div>
    </form>
  </section>
}
