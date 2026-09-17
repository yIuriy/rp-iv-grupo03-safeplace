import { useState } from 'react'
import { Alert, Button, Dialog } from '../../shared/components'
import { cadastrarColaborador, cadastrarSupervisor, type UsuarioCriado } from './api'
import { PessoaFormulario } from './PessoaFormulario'

export type TipoCadastro = 'supervisor' | 'colaborador'

const configuracao = {
  supervisor: {
    titulo: 'Novo supervisor',
    descricao: 'O Supervisor recebe conta de acesso. A senha inicial é gerada pelo sistema e exibida uma única vez, ao final do cadastro.',
    emailObrigatorio: true,
    emailHelper: 'Identificação de acesso usada no login.',
    enviarLabel: 'Cadastrar supervisor',
    cadastrar: cadastrarSupervisor,
  },
  colaborador: {
    titulo: 'Novo colaborador',
    descricao: 'O Colaborador é cadastrado para vínculo em ocorrências, capacitações e empréstimos de EPIs. Não recebe conta, senha nem acesso ao sistema.',
    emailObrigatorio: false,
    emailHelper: 'Contato opcional. O Colaborador não usa o sistema.',
    enviarLabel: 'Cadastrar colaborador',
    cadastrar: cadastrarColaborador,
  },
} satisfies Record<TipoCadastro, unknown>

/** Passo final do cadastro de Supervisor: única vez em que a senha inicial aparece (RF23, issue #91). */
function SenhaInicialPainel({ usuario, aoConcluir }: { usuario: UsuarioCriado; aoConcluir: () => void }) {
  return <div className="sp-dialog-body">
    <Alert tone="success" title="Cadastro concluído" live="polite">
      {usuario.nome} já pode entrar no SafePlace com o e-mail {usuario.email ?? 'cadastrado'}.
    </Alert>
    {usuario.senhaInicial
      ? <dl className="sp-senha-inicial">
        <dt>Senha inicial</dt>
        <dd><code>{usuario.senhaInicial}</code></dd>
      </dl>
      : <Alert tone="warning" title="Senha inicial não recebida">
        A API não devolveu a senha inicial. Confira o cadastro no servidor antes de comunicar o acesso.
      </Alert>}
    <p className="sp-dialog-note">Anote e entregue esta senha ao Supervisor agora. Por segurança, ela não é exibida novamente nem aparece em consultas.</p>
    <div className="sp-dialog-actions"><Button onClick={aoConcluir}>Concluir</Button></div>
  </div>
}

export function CadastroDialog({ tipo, open, aoFechar, aoCriado }: {
  tipo: TipoCadastro
  open: boolean
  aoFechar: () => void
  aoCriado: (usuario: UsuarioCriado) => void
}) {
  const [criado, setCriado] = useState<UsuarioCriado | null>(null)
  const dados = configuracao[tipo]

  function fechar() {
    setCriado(null)
    aoFechar()
  }

  async function enviar(pessoa: Parameters<typeof dados.cadastrar>[0]) {
    const usuario = await dados.cadastrar(pessoa)
    aoCriado(usuario)
    if (tipo === 'supervisor') setCriado(usuario)
    else fechar()
  }

  // O conteúdo só existe enquanto o diálogo está aberto: fechar descarta rascunho e estado de envio.
  return <Dialog open={open} title={criado ? 'Supervisor cadastrado' : dados.titulo}
    description={criado ? undefined : dados.descricao} onClose={fechar}>
    {open && (criado
      ? <SenhaInicialPainel usuario={criado} aoConcluir={fechar} />
      : <PessoaFormulario emailObrigatorio={dados.emailObrigatorio} emailHelper={dados.emailHelper}
        enviarLabel={dados.enviarLabel} aoEnviar={enviar} aoCancelar={fechar} />)}
  </Dialog>
}
