import { useCallback, useMemo, useState, type FormEvent } from 'react'
import { mensagemDaApi, statusDoErro } from '../../shared/api/erros'
import { useConsulta } from '../../shared/api/useConsulta'
import { Alert, Badge, Button, Dialog, EmptyState, HistoryItem, Select, Table, TableRow, TextArea, TextField, Toast } from '../../shared/components'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { listarEpis, listarHistorico, registrarManutencao, type DadosManutencao, type Epi, type StatusEpi } from './api'

const statusOperacionais: readonly StatusEpi[] = ['DISPONIVEL', 'EM_USO', 'EM_MANUTENCAO', 'ESGOTADO']
const rotulos: Record<StatusEpi, string> = {
  DISPONIVEL: 'Disponível', EM_USO: 'Em uso', EM_MANUTENCAO: 'Em manutenção', ESGOTADO: 'Esgotado', DESCARTADO: 'Descartado',
}

function tomDoStatus(status: StatusEpi): 'success' | 'info' | 'warning' | 'danger' {
  if (status === 'DISPONIVEL') return 'success'
  if (status === 'EM_MANUTENCAO') return 'warning'
  return status === 'DESCARTADO' || status === 'ESGOTADO' ? 'danger' : 'info'
}

function formatarData(data: string) {
  const valor = new Date(data)
  return Number.isNaN(valor.getTime()) ? data : valor.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

function descreverErro(erro: unknown) {
  if (statusDoErro(erro) === 403) return 'Seu perfil não tem permissão para registrar manutenção.'
  if (statusDoErro(erro) === 404) return 'EPI não encontrado. Atualize a busca e escolha outro registro.'
  return mensagemDaApi(erro) ?? 'Não foi possível registrar manutenção. Nenhuma confirmação foi exibida.'
}

function DialogoManutencao({ epi, aberto, aoFechar, aoSalvar }: {
  epi: Epi
  aberto: boolean
  aoFechar: () => void
  aoSalvar: () => void
}) {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const dados: DadosManutencao = {
      dataManutencao: String(form.get('dataManutencao') ?? ''),
      descricao: String(form.get('descricao') ?? '').trim(),
      resultado: String(form.get('resultado') ?? '') as DadosManutencao['resultado'],
    }
    setErro('')
    setEnviando(true)
    try {
      await registrarManutencao(epi.id, dados)
      aoSalvar()
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return <Dialog open={aberto} title={`Registrar manutenção: ${epi.nome}`}
    description="Informe data, descrição e resultado. O tipo de manutenção não é obrigatório no MVP." onClose={() => { if (!enviando) aoFechar() }}>
    {aberto && <form className="sp-form" onSubmit={enviar}>
      {erro && <Alert tone="danger" title="Registro não concluído" live="assertive">{erro}</Alert>}
      <TextField label="Data e hora da manutenção" name="dataManutencao" type="datetime-local" required />
      <TextArea label="Descrição da manutenção" name="descricao" required maxLength={1000}
        helper="Descreva o serviço realizado no EPI." />
      <Select label="Resultado" name="resultado" required defaultValue="">
        <option value="" disabled>Selecione o resultado</option>
        <option value="APROVADO">Aprovado</option>
        <option value="REPROVADO">Reprovado</option>
      </Select>
      <div className="sp-dialog-actions">
        <Button variant="secondary" disabled={enviando} onClick={aoFechar}>Cancelar</Button>
        <Button type="submit" loading={enviando}>Salvar manutenção</Button>
      </div>
    </form>}
  </Dialog>
}

export function EpiPage() {
  const sessao = useSessaoAtiva()
  const [filtro, setFiltro] = useState<StatusEpi | 'TODOS'>('TODOS')
  const [selecionadoId, setSelecionadoId] = useState<number | null>(null)
  const [dialogoAberto, setDialogoAberto] = useState(false)
  const [aviso, setAviso] = useState('')
  const carregarEpis = useCallback((signal: AbortSignal) => listarEpis(signal), [])
  const epis = useConsulta('epis', carregarEpis)
  const selecionado = epis.dados?.find(epi => epi.id === selecionadoId) ?? null
  const carregarHistorico = useCallback((signal: AbortSignal) =>
    selecionadoId === null ? Promise.resolve([]) : listarHistorico(selecionadoId, signal), [selecionadoId])
  const historico = useConsulta(`historico:${selecionadoId ?? 'nenhum'}`, carregarHistorico)
  const listaOperacional = useMemo(() => (epis.dados ?? [])
    .filter(epi => epi.status !== 'DESCARTADO')
    .filter(epi => filtro === 'TODOS' || epi.status === filtro), [epis.dados, filtro])
  const podeRegistrar = sessao.perfil === 'GESTOR_SEGURANCA'

  function selecionar(epi: Epi) {
    setSelecionadoId(epi.id)
    setDialogoAberto(false)
  }

  function aposSalvar() {
    setDialogoAberto(false)
    setAviso('Manutenção registrada. Situação e histórico foram atualizados.')
    epis.recarregar()
    historico.recarregar()
  }

  return <section className="sp-feature-page">
    <header className="sp-feature-heading"><h1 className="sp-type-heading-md">EPIs</h1>
      <p>Consulte EPIs ativos, filtre situação e acompanhe o histórico de manutenção.</p></header>

    <div className="sp-epi-filter">
      <Select label="Filtrar por situação" value={filtro} onChange={event => setFiltro(event.target.value as StatusEpi | 'TODOS')}>
        <option value="TODOS">Todas as situações ativas</option>
        {statusOperacionais.map(status => <option key={status} value={status}>{rotulos[status]}</option>)}
      </Select>
      <p role="status">{listaOperacional.length} {listaOperacional.length === 1 ? 'EPI ativo encontrado' : 'EPIs ativos encontrados'}.</p>
    </div>

    {epis.erro ? <Alert tone="danger" title="Não foi possível consultar os EPIs" live="assertive">
      <Button variant="secondary" onClick={epis.recarregar}>Tentar novamente</Button>
    </Alert> : epis.dados === null ? <p role="status">Carregando EPIs...</p>
      : listaOperacional.length === 0 ? <EmptyState title="Nenhum EPI encontrado"
        description="Ajuste o filtro ou atualize a listagem para pesquisar novamente." actionLabel="Atualizar busca" onAction={epis.recarregar} />
        : <Table caption="EPIs ativos disponíveis para consulta">
          <thead><tr><th scope="col">EPI</th><th scope="col">CA</th><th scope="col">Quantidade</th><th scope="col">Situação</th><th scope="col">Ação</th></tr></thead>
          <tbody>{listaOperacional.map(epi => <TableRow key={epi.id}>
            <td><strong>{epi.nome}</strong></td><td>{epi.numeroCa}</td><td>{epi.quantidade}</td>
            <td><Badge tone={tomDoStatus(epi.status)}>{rotulos[epi.status]}</Badge></td>
            <td><Button variant="secondary" onClick={() => selecionar(epi)} aria-label={`Consultar histórico de ${epi.nome}`}>Consultar</Button></td>
          </TableRow>)}</tbody>
        </Table>}

    {selecionado && <section className="sp-epi-detail" aria-labelledby="epi-detail-title">
      <div className="sp-epi-detail-heading"><div><h2 id="epi-detail-title" className="sp-type-heading-sm">{selecionado.nome}</h2>
        <p>CA {selecionado.numeroCa} · estoque: {selecionado.quantidade}</p></div>
        <Badge tone={tomDoStatus(selecionado.status)}>{rotulos[selecionado.status]}</Badge></div>
      {podeRegistrar && <Button icon="plus" onClick={() => setDialogoAberto(true)}>Registrar manutenção</Button>}
      {!podeRegistrar && <Alert tone="info" title="Consulta disponível">Somente Gestor de Segurança registra manutenção.</Alert>}
      <h3 className="sp-type-heading-sm">Histórico de manutenção</h3>
      {historico.erro ? <Alert tone="danger" title="EPI não encontrado ou histórico indisponível" live="assertive">
        <Button variant="secondary" onClick={() => { setSelecionadoId(null); epis.recarregar() }}>Nova busca</Button>
      </Alert> : historico.dados === null ? <p role="status">Carregando histórico...</p>
        : historico.dados.length === 0 ? <EmptyState title="Sem manutenções registradas" description="Este EPI ainda não possui histórico de manutenção." />
          : <ol className="sp-epi-history">{historico.dados.map(manutencao => <HistoryItem key={manutencao.id}
            dateTime={manutencao.dataManutencao} dateLabel={formatarData(manutencao.dataManutencao)}
            title={manutencao.resultado === 'APROVADO' ? 'Manutenção aprovada' : 'Manutenção reprovada'}>
            {manutencao.descricao}{manutencao.responsavelManutencao ? ` · Registrada por ${manutencao.responsavelManutencao}` : ''}
          </HistoryItem>)}</ol>}
    </section>}
    {selecionado && podeRegistrar && <DialogoManutencao epi={selecionado} aberto={dialogoAberto}
      aoFechar={() => setDialogoAberto(false)} aoSalvar={aposSalvar} />}
    <Toast message={aviso} onDismiss={() => setAviso('')} />
  </section>
}
