// Diálogo de cadastro e edição de setor físico pelo Gestor de Segurança (RF05, UC03, issue #174): nome, grau de
// perigo, localização e descrição, com validação local, foco no primeiro erro e mensagem da API.

import { useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Select, TextArea, TextField } from '../../shared/components'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'
import { atualizarSetor, cadastrarSetor, niveisPerigo, rotuloNivelPerigo, type Setor } from './api'
import {
  lerDadosSetor, ordemDosCamposSetor, TAMANHO_MAXIMO_LOCALIZACAO, TAMANHO_MAXIMO_NOME, type ErrosSetor,
} from './validacaoSetor'

const descreverFalha = criarDescritorDeFalha({
  403: 'Seu perfil não tem permissão para gerenciar setores.'
}, usarMensagemDaApi('O servidor não confirmou a operação. Verifique a conexão e tente novamente.'))

function focarCampo(form: HTMLFormElement, nome: string) {
  const campo = form.elements.namedItem(nome)
  if (campo instanceof HTMLElement) campo.focus()
}

function SetorFormulario({
  setor,
  aoSalvar,
  aoCancelar,
}: {
  setor?: Setor | null
  aoSalvar: (setor: Setor) => void
  aoCancelar: () => void
}) {
  const editando = !!setor
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
      if (editando && setor) {
        aoSalvar(await atualizarSetor(setor.id, dados))
      } else {
        aoSalvar(await cadastrarSetor(dados))
      }
    } catch (erro) {
      setFalha(descreverFalha(erro))
      setEnviando(false)
    }
  }

  const temPendencias = ordemDosCamposSetor.some(campo => erros[campo])
  return <form className="sp-form" onSubmit={enviar} noValidate>
    <TextField label="Nome do setor" name="nome" required autoComplete="off" maxLength={TAMANHO_MAXIMO_NOME}
      defaultValue={setor?.nome ?? ''} error={erros.nome} autoFocus
      helper="Use o mesmo nome informado na localização dos EPIs deste setor." />
    <Select label="Grau de perigo" name="status" required defaultValue={setor?.status ?? ''} error={erros.status}
      helper="A projeção de substituição de EPIs aplica este grau aos EPIs localizados neste setor.">
      <option value="" disabled>Selecione o grau</option>
      {niveisPerigo.map(nivel => <option key={nivel} value={nivel}>{rotuloNivelPerigo[nivel]}</option>)}
    </Select>
    <TextField label="Localização na planta (opcional)" name="localizacao" autoComplete="off"
      defaultValue={setor?.localizacao ?? ''} maxLength={TAMANHO_MAXIMO_LOCALIZACAO} error={erros.localizacao} placeholder="Ex.: Bloco 2, piso térreo" />
    <TextArea label="Descrição (opcional)" name="descricao" rows={3}
      defaultValue={setor?.descricao ?? ''} placeholder="Atividades e características do setor" />
    {temPendencias && <Alert tone="danger" title="Confira os campos destacados" live="assertive">
      Os dados preenchidos foram mantidos. Corrija os campos indicados e envie novamente.
    </Alert>}
    {falha && <Alert tone="danger" title="Não foi possível salvar o setor" live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">{editando ? 'Salvar alterações' : 'Cadastrar setor'}</Button>
    </div>
  </form>
}

/** O conteúdo só existe com o diálogo aberto: fechar descarta o rascunho e o estado de envio. */
export function SetorFormDialog({ open, setor, aoFechar, aoSalvar }: {
  open: boolean
  setor?: Setor | null
  aoFechar: () => void
  aoSalvar: (setor: Setor) => void
}) {
  const editando = !!setor
  return <Dialog open={open} title={editando ? `Editar setor: ${setor?.nome}` : "Cadastrar setor físico"} onClose={aoFechar}
    description={editando
      ? "Atualize o grau de perigo, localização e descrição do setor físico."
      : "O setor reúne áreas de risco da planta. Nome e grau de perigo são obrigatórios."}>
    {open && <SetorFormulario key={setor?.id ?? 'novo'} setor={setor} aoCancelar={aoFechar} aoSalvar={s => { aoSalvar(s); aoFechar() }} />}
  </Dialog>
}
