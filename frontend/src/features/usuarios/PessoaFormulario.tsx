// Formulário compartilhado de cadastro e edição de pessoa: nome, CPF (com máscara; somente leitura
// na edição), data de nascimento e e-mail, com validação local e foco no primeiro erro. O fluxo de envio vem
// do hook useEnvioFormulario (Template Method, #233).

import { Alert, Button, TextField } from '../../shared/components'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import { descreverFalhaDeCadastro, type DadosPessoa, type Usuario } from './api'
import { CpfField } from './CpfField'
import { hojeIso } from './formatos'
import { lerDadosPessoa, ordemDosCampos, validarPessoa, type ErrosPessoa } from './validacaoPessoa'

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
  const editando = valoresIniciais !== undefined
  const { enviando, erros, falha, temPendencias, onSubmit } = useEnvioFormulario({
    ler: form => lerDadosPessoa(new FormData(form)),
    validar: dados => {
      const pendencias = validarPessoa(dados, emailObrigatorio)
      return { dados: ordemDosCampos.some(campo => pendencias[campo]) ? null : dados, erros: pendencias }
    },
    ordem: ordemDosCampos,
    enviar: aoEnviar,
    descreverFalha: descreverFalhaDeCadastro,
  }, {} as ErrosPessoa)

  return <form className="sp-form" onSubmit={onSubmit} noValidate>
    <TextField label="Nome completo" name="nome" autoComplete="off" required error={erros.nome}
      defaultValue={valoresIniciais?.nome} />
    <CpfField label="CPF" name="cpf" required error={erros.cpf} defaultValue={valoresIniciais?.cpf} readOnly={editando}
      helper={editando ? 'O CPF identifica o cadastro e não é alterado nesta operação.' : 'Digite somente os números; a formatação é automática.'} />
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
