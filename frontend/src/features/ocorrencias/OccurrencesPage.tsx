import { useState, useTransition, type FormEvent } from 'react'
import {
  Badge,
  Button,
  EmptyState,
  SearchInput,
  Select,
  Table,
  TableRow,
} from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useConsultaOffline } from '../../shared/offline/useConsultaOffline'
import { OfflineBanner } from '../../shared/offline/OfflineBanner'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import {
  listarOcorrencias,
  rotuloStatusOcorrencia,
  type StatusOcorrencia,
} from './api'

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
}

function formatarData(iso: string): string {
  try {
    const d = new Date(iso)
    if (isNaN(d.getTime())) return iso
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(d)
  } catch {
    return iso
  }
}

export function OccurrencesPage() {
  const sessao = useSessaoAtiva()
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'

  const [busca, setBusca] = useState('')
  const [filtroTipo, setFiltroTipo] = useState<string>('')
  const [filtroStatus, setFiltroStatus] = useState<string>('')
  const [, startTransition] = useTransition()

  const consulta = useConsultaOffline('ocorrencias', listarOcorrencias)

  function aoFiltrarTexto(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    startTransition(() => {
      setBusca(String(form.get('busca') ?? ''))
    })
  }

  function limparFiltros() {
    setBusca('')
    setFiltroTipo('')
    setFiltroStatus('')
    consulta.recarregar()
  }

  return (
    <FeaturePage
      title="Ocorrências"
      description={
        gestor
          ? 'Acompanhe, faça a triagem e gerencie os acidentes e incidentes de trabalho (UC05).'
          : 'Consulte os relatos de acidentes e incidentes sob sua responsabilidade (UC09).'
      }
    >
      <div className="sp-painel">
        <OfflineBanner
          isOffline={consulta.isOffline}
          sincronizadoEm={consulta.sincronizadoEm}
        />

        <div className="sp-painel-topo">
          <form
            className="sp-busca-filtros"
            role="search"
            aria-label="Buscar e filtrar ocorrências"
            onSubmit={aoFiltrarTexto}
            onReset={limparFiltros}
          >
            <SearchInput
              label="Buscar ocorrência"
              name="busca"
              className="sp-busca-campo"
              hiddenLabel={false}
              placeholder="Descrição, protocolo ou colaborador"
              autoComplete="off"
            />
            <Select
              label="Tipo"
              name="tipoOcorrencia"
              value={filtroTipo}
              onChange={e => setFiltroTipo(e.target.value)}
            >
              <option value="">Todos os tipos</option>
              <option value="ACIDENTE">Acidente</option>
              <option value="INCIDENTE">Incidente</option>
            </Select>
            <Select
              label="Status"
              name="statusOcorrencia"
              value={filtroStatus}
              onChange={e => setFiltroStatus(e.target.value)}
            >
              <option value="">Todos os status</option>
              <option value="ABERTA">Aberta</option>
              <option value="EM_TRIAGEM">Em triagem</option>
              <option value="ARQUIVADA">Arquivada</option>
            </Select>
            <div className="sp-acoes-busca">
              <Button type="submit" variant="secondary" icon="search">
                Buscar
              </Button>
              <Button type="reset" variant="quiet">
                Limpar
              </Button>
            </div>
          </form>

          <Button
            variant="primary"
            icon="plus"
            disabled={consulta.isOffline}
            title={consulta.isOffline ? 'Registro de novos relatos indisponível em modo offline' : undefined}
            onClick={() => {
              // Mutação de abertura de relato quando conectado
            }}
          >
            Relatar ocorrência
          </Button>
        </div>

        <ResultadoConsulta
          consulta={consulta}
          carregandoLabel="Carregando ocorrências..."
          erroTitulo="Não foi possível carregar as ocorrências."
          vazio={{
            title: 'Nenhuma ocorrência registrada',
            description: 'Os acidentes e incidentes registrados aparecerão aqui.',
          }}
        >
          {itens => {
            const termo = normalizar(busca.trim())
            const filtradas = itens.filter(item => {
              if (filtroTipo && item.tipoOcorrencia !== filtroTipo) return false
              if (filtroStatus && item.statusOcorrencia !== filtroStatus) return false
              if (!termo) return true
              const texto = normalizar(
                `${item.idOcorrencia} ${item.descricao} ${item.numeroProtocoloCAT ?? ''} ${item.colaboradorNome ?? ''} ${item.areaNome ?? ''}`
              )
              return texto.includes(termo)
            })

            if (filtradas.length === 0) {
              return (
                <EmptyState
                  title="Nenhuma ocorrência encontrada"
                  description="Ajuste os filtros ou o termo de busca para localizar registros."
                  actionLabel="Limpar filtros"
                  onAction={limparFiltros}
                />
              )
            }

            return (
              <>
                <p className="sp-texto-secundario" role="status">
                  {filtradas.length}{' '}
                  {filtradas.length === 1 ? 'ocorrência encontrada' : 'ocorrências encontradas'}
                </p>

                <Table caption="Lista de ocorrências registradas">
                  <thead>
                    <tr>
                      <th scope="col">ID / Tipo</th>
                      <th scope="col">Data do Fato</th>
                      <th scope="col">Status</th>
                      <th scope="col">Colaborador</th>
                      <th scope="col">Protocolo CAT</th>
                      <th scope="col">Descrição</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtradas.map(oc => (
                      <TableRow key={oc.idOcorrencia}>
                        <td>
                          <strong>#{oc.idOcorrencia}</strong> ·{' '}
                          <Badge tone={oc.tipoOcorrencia === 'ACIDENTE' ? 'danger' : 'warning'}>
                            {oc.tipoOcorrencia === 'ACIDENTE' ? 'Acidente' : 'Incidente'}
                          </Badge>
                        </td>
                        <td>{formatarData(oc.dataOcorrencia)}</td>
                        <td>
                          <Badge tone={obterTomStatus(oc.statusOcorrencia)}>
                            {rotuloStatusOcorrencia[oc.statusOcorrencia]}
                          </Badge>
                        </td>
                        <td>{oc.colaboradorNome || 'Não informado'}</td>
                        <td>
                          {oc.numeroProtocoloCAT ? (
                            <code>{oc.numeroProtocoloCAT}</code>
                          ) : (
                            <span className="sp-texto-secundario">—</span>
                          )}
                        </td>
                        <td style={{ maxWidth: '320px', whiteSpace: 'pre-wrap' }}>
                          {oc.descricao}
                        </td>
                      </TableRow>
                    ))}
                  </tbody>
                </Table>
              </>
            )
          }}
        </ResultadoConsulta>
      </div>
    </FeaturePage>
  )
}

function obterTomStatus(status: StatusOcorrencia) {
  switch (status) {
    case 'ABERTA':
      return 'info'
    case 'EM_TRIAGEM':
      return 'warning'
    case 'ARQUIVADA':
      return 'success'
    default:
      return 'info'
  }
}
