import { useState, type FormEvent } from 'react'
import { Alert, Button, TextField } from '../../shared/components'
import { descreverFalhaDeCadastro, type DadosPessoa, type Usuario } from './api'
import { hojeIso } from './formatos'
import { lerDadosPessoa, ordemDosCampos, validarPessoa, type ErrosPessoa } from './validacaoPessoa'

function focarCampo(form: HTMLFormElement, nome: string) {
  const campo = form.elements.namedItem(nome)
  if (campo instanceof HTMLElement) campo.focus()
}

/**
 * Cadastro e edição de pessoa. Em edição, o CPF aparece somente leitura: ele identifica o cadastro
 * e sua alteração aguarda decisão do grupo (issue #122).
 */
export function PessoaFormulario({ emailObrigatorio, emailHelper, enviarLabel, valoresIniciais, aoEnviar, aoCancelar }: {
  emailObrigatorio: boolean
  emailHelper: string
  enviarLabel: string
  valoresIniciais?: Usuario
  aoEnviar: (dados: DadosPessoa) => Promise<void>
  aoCancelar: () => void
}) {
  const [erros, setErros] = useState<ErrosPessoa>({})
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)
  const editando = valoresIniciais !== undefined

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const form = event.currentTarget
    const dados = lerDadosPessoa(new FormData(form))
    const pendencias = validarPessoa(dados, emailObrigatorio)
    setErros(pendencias)
    const primeiroInvalido = ordemDosCampos.find(campo => pendencias[campo])
    if (primeiroInvalido) {
      focarCampo(form, primeiroInvalido)
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      await aoEnviar(dados)
    } catch (erro) {
      setFalha(descreverFalhaDeCadastro(erro))
      setEnviando(false)
    }
  }

  const temPendencias = ordemDosCampos.some(campo => erros[campo])
  return <form className="sp-form" onSubmit={enviar} noValidate>
    <TextField label="Nome completo" name="nome" autoComplete="off" required error={erros.nome}
      defaultValue={valoresIniciais?.nome} />
    <TextField label="CPF" name="cpf" inputMode="numeric" autoComplete="off" required error={erros.cpf}
      defaultValue={valoresIniciais?.cpf} readOnly={editando}
      helper={editando ? 'O CPF identifica o cadastro e não é alterado nesta operação.' : 'Somente números ou com pontos e traço.'} />
    <TextField label="Data de nascimento" name="dataNascimento" type="date" required max={hojeIso()}
      error={erros.dataNascimento} defaultValue={valoresIniciais?.dataNascimento} />
    <TextField label={emailObrigatorio ? 'E-mail corporativo' : 'E-mail de contato (opcional)'} name="email"
      type="email" autoComplete="off" required={emailObrigatorio} helper={emailHelper} error={erros.email}
      defaultValue={valoresIniciais?.email ?? undefined} />
    {temPendencias && <Alert tone="danger" title="Confira os campos destacados" live="assertive">
      Os dados preenchidos foram mantidos. Corrija os campos indicados e envie novamente.
    </Alert>}
    {falha && <Alert tone="danger" title={editando ? 'Não foi possível salvar as alterações' : 'Não foi possível concluir o cadastro'} live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">{enviarLabel}</Button>
    </div>
  </form>
}
