import { useState } from 'react'
import { Alert, Button, Dialog } from '../../shared/components'
import {
  atualizarColaborador, atualizarSupervisor, cadastrarColaborador, cadastrarSupervisor,
  type DadosPessoa, type Usuario, type UsuarioCriado,
} from './api'
import { PessoaFormulario } from './PessoaFormulario'

export type TipoCadastro = 'supervisor' | 'colaborador'
export type ModoSalvamento = 'criado' | 'atualizado'

const configuracao = {
  supervisor: {
    titulo: 'Novo supervisor',
    descricao: 'O Supervisor recebe conta de acesso. A senha inicial é gerada pelo sistema e exibida uma única vez, ao final do cadastro.',
    tituloEdicao: 'Editar supervisor',
    descricaoEdicao: 'Altere nome, data de nascimento ou e-mail de acesso. CPF, perfil e senha não mudam por aqui.',
    emailObrigatorio: true,
    emailHelper: 'Identificação de acesso usada no login.',
    enviarLabel: 'Cadastrar supervisor',
    cadastrar: cadastrarSupervisor,
    atualizar: atualizarSupervisor,
  },
  colaborador: {
    titulo: 'Novo colaborador',
    descricao: 'O Colaborador é cadastrado para vínculo em ocorrências, capacitações e empréstimos de EPIs. Não recebe conta, senha nem acesso ao sistema.',
    tituloEdicao: 'Editar colaborador',
    descricaoEdicao: 'Altere nome, data de nascimento ou e-mail de contato. O Colaborador continua sem conta de acesso e o CPF não muda por aqui.',
    emailObrigatorio: false,
    emailHelper: 'Contato opcional. O Colaborador não usa o sistema.',
    enviarLabel: 'Cadastrar colaborador',
    cadastrar: cadastrarColaborador,
    atualizar: atualizarColaborador,
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

/**
 * Cadastro (sem `usuario`) ou edição (com `usuario`) de Supervisor ou Colaborador. Na edição, o
 * corpo enviado não leva CPF: a API recusa esse campo até haver decisão do grupo (issue #122).
 */
export function CadastroDialog({ tipo, open, usuario, aoFechar, aoSalvar }: {
  tipo: TipoCadastro
  open: boolean
  usuario?: Usuario
  aoFechar: () => void
  aoSalvar: (usuario: UsuarioCriado, modo: ModoSalvamento) => void
}) {
  const [criado, setCriado] = useState<UsuarioCriado | null>(null)
  const dados = configuracao[tipo]
  const editando = usuario !== undefined

  function fechar() {
    setCriado(null)
    aoFechar()
  }

  async function enviar(pessoa: DadosPessoa) {
    if (editando) {
      const editaveis = { nome: pessoa.nome, dataNascimento: pessoa.dataNascimento, email: pessoa.email }
      aoSalvar(await dados.atualizar(usuario.id, editaveis), 'atualizado')
      fechar()
      return
    }
    const salvo = await dados.cadastrar(pessoa)
    aoSalvar(salvo, 'criado')
    if (tipo === 'supervisor') setCriado(salvo)
    else fechar()
  }

  const titulo = criado ? 'Supervisor cadastrado' : editando ? dados.tituloEdicao : dados.titulo
  const descricao = criado ? undefined : editando ? dados.descricaoEdicao : dados.descricao

  // O conteúdo só existe enquanto o diálogo está aberto: fechar descarta rascunho e estado de envio.
  return <Dialog open={open} title={titulo} description={descricao} onClose={fechar}>
    {open && (criado
      ? <SenhaInicialPainel usuario={criado} aoConcluir={fechar} />
      : <PessoaFormulario emailObrigatorio={dados.emailObrigatorio} emailHelper={dados.emailHelper}
        enviarLabel={editando ? 'Salvar alterações' : dados.enviarLabel} valoresIniciais={usuario}
        aoEnviar={enviar} aoCancelar={fechar} />)}
  </Dialog>
}
