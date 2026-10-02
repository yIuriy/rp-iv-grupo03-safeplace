// Diálogo de registro de inspeção pelo Gestor de Segurança (RF09, US09, issue #136): data e itens
// verificados, cada um com referência avaliada, resultado e observações, com validação local, foco no
// primeiro erro e mensagem da API. Inspeção registrada não é editada depois.

import { useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Radio, TextArea, TextField } from '../../shared/components'
import { mensagemDaApi, statusDoErro } from '../../shared/api/erros'
import { registrarInspecao, type Inspecao } from './apiInspecoes'
import { hojeIso, validarInspecao, type ErrosInspecao, type ErrosItem, type RascunhoItem } from './validacaoInspecao'

function descreverFalha(erro: unknown): string {
  if (statusDoErro(erro) === 403) return 'Seu perfil não tem permissão para registrar inspeções.'
  return mensagemDaApi(erro) ?? 'O servidor não confirmou o registro. Verifique a conexão e tente novamente.'
}

function itemVazio(chave: number): RascunhoItem {
  return { chave, descricao: '', resultado: '', observacoes: '' }
}

function primeiroCampoInvalido(erros: ErrosInspecao, itens: RascunhoItem[]): string | null {
  if (erros.dataInspecao) return 'data-inspecao'
  for (const item of itens) {
    const errosItem = erros.itens[item.chave]
    if (errosItem?.descricao) return `item-${item.chave}-descricao`
    if (errosItem?.resultado) return `item-${item.chave}-conforme`
  }
  return null
}

function ItemCampos({ item, numero, erros, podeRemover, aoAlterar, aoRemover }: {
  item: RascunhoItem
  numero: number
  erros?: ErrosItem
  podeRemover: boolean
  aoAlterar: (alteracao: Partial<RascunhoItem>) => void
  aoRemover: () => void
}) {
  const idErroResultado = `item-${item.chave}-resultado-erro`
  return <fieldset className="sp-inspecao-item">
    <legend className="sp-field-label">Item verificado {numero}</legend>
    <TextField id={`item-${item.chave}-descricao`} label="Norma ou referência avaliada" value={item.descricao}
      onChange={e => aoAlterar({ descricao: e.target.value })} error={erros?.descricao} autoComplete="off"
      placeholder="Ex.: NR-26, sinalização de segurança" />
    <fieldset className="sp-inspecao-resultado" aria-describedby={erros?.resultado ? idErroResultado : undefined}>
      <legend className="sp-field-label">Resultado</legend>
      <Radio id={`item-${item.chave}-conforme`} name={`resultado-${item.chave}`} label="Conformidade"
        checked={item.resultado === 'conforme'} onChange={() => aoAlterar({ resultado: 'conforme' })} />
      <Radio name={`resultado-${item.chave}`} label="Não conformidade"
        checked={item.resultado === 'nao-conforme'} onChange={() => aoAlterar({ resultado: 'nao-conforme' })} />
      {erros?.resultado && <p id={idErroResultado} className="sp-field-message sp-field-message--error">
        {erros.resultado}
      </p>}
    </fieldset>
    <TextArea label="Observações (opcional)" rows={2} value={item.observacoes}
      onChange={e => aoAlterar({ observacoes: e.target.value })}
      placeholder="Condições verificadas e descrição da não conformidade" />
    {podeRemover && <div>
      <Button variant="quiet" icon="x" onClick={aoRemover}>Remover item {numero}</Button>
    </div>}
  </fieldset>
}

function InspecaoFormulario({ areaId, aoSalvar, aoCancelar }: {
  areaId: number
  aoSalvar: (inspecao: Inspecao) => void
  aoCancelar: () => void
}) {
  const hoje = hojeIso()
  const [dataInspecao, setDataInspecao] = useState(hoje)
  const [itens, setItens] = useState<RascunhoItem[]>([itemVazio(1)])
  const [proximaChave, setProximaChave] = useState(2)
  const [erros, setErros] = useState<ErrosInspecao>({ itens: {} })
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)

  function alterarItem(chave: number, alteracao: Partial<RascunhoItem>) {
    setItens(atuais => atuais.map(item => (item.chave === chave ? { ...item, ...alteracao } : item)))
  }

  function adicionarItem() {
    setItens(atuais => [...atuais, itemVazio(proximaChave)])
    setProximaChave(chave => chave + 1)
  }

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const { dados, erros: pendencias } = validarInspecao(dataInspecao, itens, hoje)
    setErros(pendencias)
    if (!dados) {
      const campo = primeiroCampoInvalido(pendencias, itens)
      if (campo) document.getElementById(campo)?.focus()
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      aoSalvar(await registrarInspecao(areaId, dados))
    } catch (erro) {
      setFalha(descreverFalha(erro))
      setEnviando(false)
    }
  }

  const temPendencias = Boolean(erros.dataInspecao) || Object.keys(erros.itens).length > 0
  return <form className="sp-form" onSubmit={enviar} noValidate>
    <TextField id="data-inspecao" label="Data da inspeção" type="date" max={hoje} required value={dataInspecao}
      onChange={e => setDataInspecao(e.target.value)} error={erros.dataInspecao}
      helper="Dia em que a inspeção foi realizada; não pode ser posterior a hoje." />
    {itens.map((item, indice) => <ItemCampos key={item.chave} item={item} numero={indice + 1}
      erros={erros.itens[item.chave]} podeRemover={itens.length > 1}
      aoAlterar={alteracao => alterarItem(item.chave, alteracao)}
      aoRemover={() => setItens(atuais => atuais.filter(atual => atual.chave !== item.chave))} />)}
    <div>
      <Button variant="secondary" icon="plus" onClick={adicionarItem}>Adicionar item verificado</Button>
    </div>
    {temPendencias && <Alert tone="danger" title="Confira os campos destacados" live="assertive">
      Os dados preenchidos foram mantidos. Corrija os campos indicados e envie novamente.
    </Alert>}
    {falha && <Alert tone="danger" title="Não foi possível registrar a inspeção" live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">Registrar inspeção</Button>
    </div>
  </form>
}

/** O conteúdo só existe com o diálogo aberto: fechar descarta o rascunho e o estado de envio. */
export function InspecaoFormDialog({ area, aoFechar, aoSalvar }: {
  area: { id: number; codigo: string; nome: string } | null
  aoFechar: () => void
  aoSalvar: (inspecao: Inspecao) => void
}) {
  return <Dialog open={area !== null} onClose={aoFechar}
    title={area ? `Registrar inspeção: ${area.codigo}` : 'Registrar inspeção'}
    description="A inspeção entra no histórico da área e não é alterada depois. Informe ao menos um item verificado.">
    {area && <InspecaoFormulario areaId={area.id} aoCancelar={aoFechar}
      aoSalvar={inspecao => { aoSalvar(inspecao); aoFechar() }} />}
  </Dialog>
}
