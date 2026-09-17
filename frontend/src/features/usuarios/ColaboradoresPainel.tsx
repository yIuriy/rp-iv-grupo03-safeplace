import { useCallback, useState, type FormEvent } from 'react'
import { Button, SearchInput, TextField, Toast } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { listarColaboradores, type FiltroColaboradores } from './api'
import { CadastroDialog } from './CadastroDialog'
import { ResultadoConsulta } from './ResultadoConsulta'
import { UsuariosTabela } from './UsuariosTabela'

function lerFiltro(form: FormData): FiltroColaboradores {
  const nome = String(form.get('nome') ?? '').trim()
  const cpf = String(form.get('cpf') ?? '').replace(/\D/g, '')
  return { nome: nome || undefined, cpf: cpf || undefined }
}

/** Supervisor e Gestor cadastram e consultam colaboradores, que não têm conta de acesso (RF23). */
export function ColaboradoresPainel() {
  const [filtro, setFiltro] = useState<FiltroColaboradores>({})
  const [cadastroAberto, setCadastroAberto] = useState(false)
  const [aviso, setAviso] = useState('')
  const carregar = useCallback((signal: AbortSignal) => listarColaboradores(filtro, signal), [filtro])
  const consulta = useConsulta(JSON.stringify(filtro), carregar)
  const filtrando = Boolean(filtro.nome || filtro.cpf)

  function buscar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFiltro(lerFiltro(new FormData(event.currentTarget)))
  }

  return <div className="sp-painel">
    <form className="sp-busca" role="search" aria-label="Buscar colaboradores" onSubmit={buscar} onReset={() => setFiltro({})}>
      <SearchInput label="Nome" name="nome" hiddenLabel={false} placeholder="Parte do nome" autoComplete="off" />
      <TextField label="CPF" name="cpf" inputMode="numeric" placeholder="Somente números" autoComplete="off" />
      <Button type="submit" variant="secondary" icon="search">Buscar</Button>
      <Button type="reset" variant="quiet">Limpar</Button>
    </form>
    <div className="sp-painel-acoes">
      <p>Colaboradores são vinculados a ocorrências, capacitações e empréstimos. Não recebem conta nem senha.</p>
      <Button icon="plus" onClick={() => setCadastroAberto(true)}>Novo colaborador</Button>
    </div>
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando colaboradores..."
      erroTitulo="Não foi possível carregar os colaboradores."
      vazio={filtrando
        ? { title: 'Nenhum colaborador encontrado', description: 'Nenhum cadastro corresponde ao nome ou CPF informados. Ajuste a busca ou limpe os filtros.' }
        : { title: 'Nenhum colaborador cadastrado', description: 'Cadastre o primeiro colaborador para vinculá-lo aos registros do sistema.' }}>
      {colaboradores => <UsuariosTabela caption="Colaboradores cadastrados" usuarios={colaboradores} />}
    </ResultadoConsulta>
    <CadastroDialog tipo="colaborador" open={cadastroAberto} aoFechar={() => setCadastroAberto(false)}
      aoCriado={usuario => { setAviso(`${usuario.nome} cadastrado sem conta de acesso.`); consulta.recarregar() }} />
    <Toast message={aviso} onDismiss={() => setAviso('')} />
  </div>
}
