// Painel de empréstimos (RF11; UC12; US11): filtro por situação na API (em posse, atrasados ou todos),
// busca local por colaborador ou EPI, novo empréstimo, detalhes e devolução. Supervisor e Gestor
// operam do mesmo modo, como no SecurityConfig. Sem cópia offline: a #105 não aprova cache de
// empréstimos.

import { useCallback, useState, type FormEvent } from 'react'
import { Button, SearchInput, Select, Toast } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { normalizar } from '../../shared/formatos'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import { listarEmprestimos, type Emprestimo, type SituacaoConsulta } from './api'
import { DevolucaoDialog } from './DevolucaoDialog'
import { EmprestimoDetalheDialog } from './EmprestimoDetalheDialog'
import { EmprestimosTabela } from './EmprestimosTabela'
import { EntregaDialog } from './EntregaDialog'
import { mensagemDeDevolucao } from './formatos'

const situacoes: { valor: SituacaoConsulta; rotulo: string; vazio: { title: string; description: string } }[] = [
  {
    valor: 'PENDENTES', rotulo: 'Em posse',
    vazio: { title: 'Nenhum EPI em posse', description: 'Os empréstimos ativos aparecerão aqui com o colaborador e o prazo de devolução.' },
  },
  {
    valor: 'ATRASADOS', rotulo: 'Atrasados',
    vazio: { title: 'Nenhum empréstimo atrasado', description: 'Empréstimos sem devolução depois da data prevista aparecerão aqui.' },
  },
  {
    valor: 'TODOS', rotulo: 'Todos, incluindo devolvidos',
    vazio: { title: 'Nenhum empréstimo registrado', description: 'As entregas e devoluções registradas aparecerão aqui.' },
  },
]

function filtrar(emprestimos: Emprestimo[], termo: string): Emprestimo[] {
  const busca = normalizar(termo.trim())
  if (!busca) return emprestimos
  return emprestimos.filter(emprestimo => normalizar(`${emprestimo.colaboradorNome} ${emprestimo.epiNome}`).includes(busca))
}

function resumoDosAtrasos(emprestimos: Emprestimo[] | null): string {
  const atrasados = emprestimos?.filter(emprestimo => emprestimo.atrasado && emprestimo.status !== 'DISPONIVEL').length ?? 0
  if (atrasados === 0) return ''
  return atrasados === 1 ? '1 empréstimo com devolução atrasada.' : `${atrasados} empréstimos com devolução atrasada.`
}

export function EmprestimosPainel() {
  const [situacao, setSituacao] = useState<SituacaoConsulta>('PENDENTES')
  const [termo, setTermo] = useState('')
  const [entregaAberta, setEntregaAberta] = useState(false)
  const [detalhando, setDetalhando] = useState<Emprestimo | null>(null)
  const [devolvendo, setDevolvendo] = useState<Emprestimo | null>(null)
  const [aviso, setAviso] = useState('')
  const carregar = useCallback((signal: AbortSignal) => listarEmprestimos(situacao, signal), [situacao])
  const consulta = useConsulta(situacao, carregar)
  const atual = situacoes.find(opcao => opcao.valor === situacao) ?? situacoes[0]
  const atrasos = resumoDosAtrasos(consulta.dados)

  function buscar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setTermo(String(new FormData(event.currentTarget).get('busca') ?? ''))
  }

  function aoRegistrar(emprestimo: Emprestimo) {
    setAviso(`Entrega registrada: ${emprestimo.epiNome} para ${emprestimo.colaboradorNome}.`)
    consulta.recarregar()
  }

  function aoDevolver(emprestimo: Emprestimo) {
    setAviso(mensagemDeDevolucao(emprestimo))
    consulta.recarregar()
  }

  return <div className="sp-painel">
    <div className="sp-painel-acoes">
      <p>A entrega retém uma unidade do EPI. Só o item devolvido apto volta ao saldo disponível.</p>
      <Button icon="plus" onClick={() => setEntregaAberta(true)}>Novo empréstimo</Button>
    </div>
    <form className="sp-busca" role="search" aria-label="Buscar empréstimos" onSubmit={buscar}
      onReset={() => {
        setTermo('')
        setSituacao('PENDENTES')
      }}>
      <SearchInput label="Colaborador ou EPI" name="busca" hiddenLabel={false} placeholder="Parte do nome" autoComplete="off" />
      <Select label="Situação" name="situacao" value={situacao}
        onChange={event => setSituacao(event.target.value as SituacaoConsulta)}>
        {situacoes.map(opcao => <option key={opcao.valor} value={opcao.valor}>{opcao.rotulo}</option>)}
      </Select>
      <Button type="submit" variant="secondary" icon="search">Buscar</Button>
      <Button type="reset" variant="quiet">Limpar</Button>
    </form>
    {atrasos && <p role="status" className="sp-texto-secundario">{atrasos}</p>}
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando empréstimos..."
      erroTitulo="Não foi possível carregar os empréstimos." vazio={atual.vazio}>
      {emprestimos => {
        const visiveis = filtrar(emprestimos, termo)
        return visiveis.length === 0
          ? <p role="status">Nenhum empréstimo corresponde à busca. Ajuste o termo ou limpe o filtro.</p>
          : <EmprestimosTabela emprestimos={visiveis} aoDetalhar={setDetalhando} aoDevolver={setDevolvendo} />
      }}
    </ResultadoConsulta>
    <EntregaDialog aberto={entregaAberta} aoFechar={() => setEntregaAberta(false)} aoRegistrar={aoRegistrar} />
    <DevolucaoDialog emprestimo={devolvendo} aoFechar={() => setDevolvendo(null)} aoDevolver={aoDevolver} />
    <EmprestimoDetalheDialog emprestimo={detalhando} aoFechar={() => setDetalhando(null)} />
    <Toast message={aviso} onDismiss={() => setAviso('')} />
  </div>
}
