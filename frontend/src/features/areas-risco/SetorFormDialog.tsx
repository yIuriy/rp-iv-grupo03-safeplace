// Diálogo de cadastro de setor físico pelo Gestor de Segurança (UC03, issue #174): nome, grau de
// perigo, localização e descrição, com validação local, foco no primeiro erro e mensagem da API.

import { useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Select, TextArea, TextField } from '../../shared/components'
import { mensagemDaApi, statusDoErro } from '../../shared/api/erros'
import { cadastrarSetor, niveisPerigo, rotuloNivelPerigo, type Setor } from './api'
import {
  lerDadosSetor, ordemDosCamposSetor, TAMANHO_MAXIMO_LOCALIZACAO, TAMANHO_MAXIMO_NOME, type ErrosSetor,
} from './validacaoSetor'

function descreverFalha(erro: unknown): string {
  if (statusDoErro(erro) === 403) return 'Seu perfil não tem permissão para cadastrar setores.'
  return mensagemDaApi(erro) ?? 'O servidor não confirmou o cadastro. Verifique a conexão e tente novamente.'
}

function focarCampo(form: HTMLFormElement, nome: string) {
  const campo = form.elements.namedItem(nome)
  if (campo instanceof HTMLElement) campo.focus()
}

function SetorFormulario({ aoSalvar, aoCancelar }: { aoSalvar: (setor: Setor) => void; aoCancelar: () => void }) {
  const [erros, setErros] = useState<ErrosSetor>({})
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const form = event.currentTarget
    const { dados, erros: pendencias } = lerDadosSetor(new FormData(form))
    setErros(pendencias)
    const primeiroInvalido = ordemDosCamposSetor.find(campo => pendencias[campo])
    if (primeiroInvalido || !dados) {
      if (primeiroInvalido) focarCampo(form, primeiroInvalido)
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      aoSalvar(await cadastrarSetor(dados))
    } catch (erro) {
      setFalha(descreverFalha(erro))
      setEnviando(false)
    }
  }

  const temPendencias = ordemDosCamposSetor.some(campo => erros[campo])
  return <form className="sp-form" onSubmit={enviar} noValidate>
    <TextField label="Nome do setor" name="nome" required autoComplete="off" maxLength={TAMANHO_MAXIMO_NOME}
      error={erros.nome} autoFocus
      helper="Use o mesmo nome informado na localização dos EPIs deste setor." />
    <Select label="Grau de perigo" name="status" required defaultValue="" error={erros.status}
      helper="A projeção de substituição de EPIs (UC10) aplica este grau aos EPIs localizados neste setor.">
      <option value="" disabled>Selecione o grau</option>
      {niveisPerigo.map(nivel => <option key={nivel} value={nivel}>{rotuloNivelPerigo[nivel]}</option>)}
    </Select>
    <TextField label="Localização na planta (opcional)" name="localizacao" autoComplete="off"
      maxLength={TAMANHO_MAXIMO_LOCALIZACAO} error={erros.localizacao} placeholder="Ex.: Bloco 2, piso térreo" />
    <TextArea label="Descrição (opcional)" name="descricao" rows={3}
      placeholder="Atividades e características do setor" />
    {temPendencias && <Alert tone="danger" title="Confira os campos destacados" live="assertive">
      Os dados preenchidos foram mantidos. Corrija os campos indicados e envie novamente.
    </Alert>}
    {falha && <Alert tone="danger" title="Não foi possível cadastrar o setor" live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">Cadastrar setor</Button>
    </div>
  </form>
}

/** O conteúdo só existe com o diálogo aberto: fechar descarta o rascunho e o estado de envio. */
export function SetorFormDialog({ open, aoFechar, aoSalvar }: {
  open: boolean
  aoFechar: () => void
  aoSalvar: (setor: Setor) => void
}) {
  return <Dialog open={open} title="Cadastrar setor físico" onClose={aoFechar}
    description="O setor reúne áreas de risco da planta. Nome e grau de perigo são obrigatórios.">
    {open && <SetorFormulario aoCancelar={aoFechar} aoSalvar={setor => { aoSalvar(setor); aoFechar() }} />}
  </Dialog>
}
