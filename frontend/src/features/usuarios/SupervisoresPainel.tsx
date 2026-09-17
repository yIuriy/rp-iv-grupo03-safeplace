import { useState } from 'react'
import { Button, Toast } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { listarSupervisores, type Usuario } from './api'
import { CadastroDialog } from './CadastroDialog'
import { ResultadoConsulta } from './ResultadoConsulta'
import { UsuariosTabela } from './UsuariosTabela'

/** Só o Gestor de Segurança vê este painel; o backend aplica a mesma regra nas rotas de supervisores. */
export function SupervisoresPainel() {
  const [cadastroAberto, setCadastroAberto] = useState(false)
  const [editando, setEditando] = useState<Usuario | null>(null)
  const [aviso, setAviso] = useState('')
  const consulta = useConsulta('supervisores', listarSupervisores)

  function fecharDialogo() {
    setCadastroAberto(false)
    setEditando(null)
  }

  return <div className="sp-painel">
    <div className="sp-painel-acoes">
      <p>Supervisores têm conta de acesso e gerenciam os colaboradores.</p>
      <Button icon="plus" onClick={() => setCadastroAberto(true)}>Novo supervisor</Button>
    </div>
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando supervisores..."
      erroTitulo="Não foi possível carregar os supervisores."
      vazio={{ title: 'Nenhum supervisor cadastrado', description: 'Cadastre o primeiro supervisor para que ele possa acessar o SafePlace.' }}>
      {supervisores => <UsuariosTabela caption="Supervisores cadastrados" usuarios={supervisores} aoEditar={setEditando} />}
    </ResultadoConsulta>
    <CadastroDialog tipo="supervisor" open={cadastroAberto || editando !== null} usuario={editando ?? undefined}
      aoFechar={fecharDialogo}
      aoSalvar={(usuario, modo) => { if (modo === 'atualizado') setAviso(`${usuario.nome} atualizado.`); consulta.recarregar() }} />
    <Toast message={aviso} onDismiss={() => setAviso('')} />
  </div>
}
