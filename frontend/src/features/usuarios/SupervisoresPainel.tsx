import { useState } from 'react'
import { Button } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { listarSupervisores } from './api'
import { CadastroDialog } from './CadastroDialog'
import { ResultadoConsulta } from './ResultadoConsulta'
import { UsuariosTabela } from './UsuariosTabela'

/** Só o Gestor de Segurança vê este painel; o backend aplica a mesma regra em `POST /usuarios/supervisores`. */
export function SupervisoresPainel() {
  const [cadastroAberto, setCadastroAberto] = useState(false)
  const consulta = useConsulta('supervisores', listarSupervisores)

  return <div className="sp-painel">
    <div className="sp-painel-acoes">
      <p>Supervisores têm conta de acesso e gerenciam os colaboradores.</p>
      <Button icon="plus" onClick={() => setCadastroAberto(true)}>Novo supervisor</Button>
    </div>
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando supervisores..."
      erroTitulo="Não foi possível carregar os supervisores."
      vazio={{ title: 'Nenhum supervisor cadastrado', description: 'Cadastre o primeiro supervisor para que ele possa acessar o SafePlace.' }}>
      {supervisores => <UsuariosTabela caption="Supervisores cadastrados" usuarios={supervisores} />}
    </ResultadoConsulta>
    <CadastroDialog tipo="supervisor" open={cadastroAberto} aoFechar={() => setCadastroAberto(false)}
      aoCriado={consulta.recarregar} />
  </div>
}
