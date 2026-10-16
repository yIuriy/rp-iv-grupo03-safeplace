// Aba de tarefas (RF06, UC11): matriz de periculosidade com busca e filtro por grau. O Gestor
// cadastra, classifica e consulta o histórico de cada tarefa (#114); o Supervisor só consulta.

import { useState, useTransition, type FormEvent } from 'react'
import {
  Badge,
  Button,
  EmptyState,
  SearchInput,
  Select,
  Table,
  TableRow,
  Toast,
} from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useConsulta } from '../../shared/api/useConsulta'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { tomDoNivelPerigo } from '../areas-risco/api'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import {
  listarTarefas,
  niveisPerigo,
  rotuloNivelPerigo,
  type Tarefa,
} from './api'
import { ClassificarTarefaDialog } from './ClassificarTarefaDialog'
import { ExigenciasTarefaDialog } from './ExigenciasTarefaDialog'
import { HistoricoTarefaDialog } from './HistoricoTarefaDialog'
import { TarefaFormDialog } from './TarefaFormDialog'
import { VincularEpisTarefaDialog } from './VincularEpisTarefaDialog'
import './tarefas.css'

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
}

function formatarDataHora(iso: string | null): string {
  if (!iso) return '—'
  const [data, hora] = iso.split('T')
  if (!data || !hora) return iso
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano} ${hora.slice(0, 5)}`
}

export function TasksPage() {
  const sessao = useSessaoAtiva()
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'

  const [busca, setBusca] = useState('')
  const [filtroNivel, setFiltroNivel] = useState<string>('')
  const [, startTransition] = useTransition()

  const [formularioAberto, setFormularioAberto] = useState(false)
  const [tarefaEmEdicao, setTarefaEmEdicao] = useState<Tarefa | null>(null)
  const [tarefaExigencias, setTarefaExigencias] = useState<Tarefa | null>(null)
  const [tarefaClassificando, setTarefaClassificando] = useState<Tarefa | null>(null)
  const [tarefaEpis, setTarefaEpis] = useState<Tarefa | null>(null)
  const [tarefaHistorico, setTarefaHistorico] = useState<Tarefa | null>(null)
  const [mensagemSucesso, setMensagemSucesso] = useState('')

  const consultaTarefas = useConsulta('tarefas', listarTarefas)

  function aoFiltrarTexto(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    startTransition(() => {
      setBusca(String(form.get('busca') ?? ''))
    })
  }

  function limparFiltros() {
    setBusca('')
    setFiltroNivel('')
    consultaTarefas.recarregar()
  }

  function aoSalvarNovaTarefa(tarefa: Tarefa) {
    setMensagemSucesso(`Tarefa cadastrada com sucesso: "${tarefa.descricao}".`)
    consultaTarefas.recarregar()
  }

  function aoSalvarClassificacao(tarefa: Tarefa) {
    const grau = tarefa.nivelPerigo ? rotuloNivelPerigo[tarefa.nivelPerigo] : ''
    setMensagemSucesso(`Grau de periculosidade atualizado para ${grau}.`)
    consultaTarefas.recarregar()
  }

  function aoSalvarEpis(tarefa: Tarefa) {
    const qtd = tarefa.episIds?.length ?? 0
    setMensagemSucesso(
      `EPIs da tarefa atualizados com sucesso (${qtd} vinculado${qtd === 1 ? '' : 's'}).`
    )
    consultaTarefas.recarregar()
  }

  return (
    <FeaturePage
      title="Tarefas"
      description={
        gestor
          ? 'Consulte a matriz de periculosidade, cadastre atividades operacionais e defina o grau de risco (RF06, UC11).'
          : 'Consulte a matriz de periculosidade e as diretrizes de risco das atividades operacionais (RF06, UC11).'
      }
    >
      <div className="sp-painel">
        <div className="sp-painel-topo">
          <form
            className="sp-busca-filtros"
            role="search"
            aria-label="Buscar e filtrar tarefas operacionais"
            onSubmit={aoFiltrarTexto}
            onReset={limparFiltros}
          >
            <SearchInput
              label="Buscar tarefa"
              name="busca"
              className="sp-busca-campo"
              hiddenLabel={false}
              placeholder="Parte da descrição da atividade"
              autoComplete="off"
            />
            <Select
              label="Grau de perigo"
              name="nivelPerigo"
              value={filtroNivel}
              onChange={e => setFiltroNivel(e.target.value)}
            >
              <option value="">Todos os graus</option>
              {niveisPerigo.map(nivel => (
                <option key={nivel} value={nivel}>
                  {rotuloNivelPerigo[nivel]}
                </option>
              ))}
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

          {gestor && (
            <Button
              variant="primary"
              icon="plus"
              onClick={() => setFormularioAberto(true)}
            >
              Nova tarefa
            </Button>
          )}
        </div>

        <ResultadoConsulta
          consulta={consultaTarefas}
          carregandoLabel="Carregando tarefas..."
          erroTitulo="Não foi possível carregar a matriz de tarefas."
          vazio={{
            title: 'Nenhuma tarefa cadastrada',
            description: 'As tarefas operacionais cadastradas aparecerão aqui.',
          }}
        >
          {tarefas => {
            const termo = normalizar(busca.trim())
            const filtradas = tarefas.filter(t => {
              if (filtroNivel && t.nivelPerigo !== filtroNivel) return false
              if (!termo) return true
              return normalizar(t.descricao).includes(termo)
            })

            if (filtradas.length === 0) {
              return (
                <EmptyState
                  title="Nenhuma tarefa encontrada"
                  description="Ajuste o termo de busca ou selecione outro grau de perigo."
                  actionLabel="Limpar filtros"
                  onAction={limparFiltros}
                />
              )
            }

            return (
              <>
                <p className="sp-texto-secundario" role="status">
                  {filtradas.length}{' '}
                  {filtradas.length === 1 ? 'tarefa encontrada' : 'tarefas encontradas'}
                </p>
                <Table caption="Matriz de tarefas e classificação de periculosidade">
                  <thead>
                    <tr>
                      <th scope="col">Atividade operacional</th>
                      <th scope="col">Grau de perigo</th>
                      <th scope="col">Situação</th>
                      <th scope="col">Última avaliação</th>
                      <th scope="col">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtradas.map(tarefa => (
                      <TableRow key={tarefa.id}>
                        <td>
                          <strong>{tarefa.descricao}</strong>
                          {tarefa.episIds && tarefa.episIds.length > 0 && (
                            <span
                              className="sp-texto-secundario"
                              style={{ display: 'block', fontSize: '0.8125rem', marginTop: '2px' }}
                            >
                              {tarefa.episIds.length}{' '}
                              {tarefa.episIds.length === 1 ? 'EPI obrigatório' : 'EPIs obrigatórios'}
                            </span>
                          )}
                        </td>
                        <td>
                          {tarefa.nivelPerigo ? (
                            <Badge tone={tomDoNivelPerigo[tarefa.nivelPerigo]}>
                              {rotuloNivelPerigo[tarefa.nivelPerigo]}
                            </Badge>
                          ) : (
                            <span className="sp-texto-secundario">Pendente de avaliação</span>
                          )}
                        </td>
                        <td>
                          {tarefa.classificada ? (
                            <span className="sp-tag-status sp-tag-status--ativa">Classificada</span>
                          ) : (
                            <span className="sp-tag-status sp-tag-status--pendente">
                              Não classificada
                            </span>
                          )}
                        </td>
                        <td>
                          <time dateTime={tarefa.dataClassificacao ?? undefined}>
                            {formatarDataHora(tarefa.dataClassificacao)}
                          </time>
                          {tarefa.classificada && (
                            <span className="sp-texto-secundario sp-responsavel-classificacao">
                              {tarefa.responsavelClassificacaoNome
                                ? `por ${tarefa.responsavelClassificacaoNome}`
                                : 'Responsável não registrado'}
                            </span>
                          )}
                        </td>
                        <td>
                          <div className="sp-acoes-tarefa">
                            <Button
                              variant="secondary"
                              onClick={() => setTarefaExigencias(tarefa)}
                              aria-label={`Ver exigências da tarefa: ${tarefa.descricao}`}
                            >
                              Exigências
                            </Button>
                            {gestor && (
                              <>
                                <Button
                                  variant="secondary"
                                  onClick={() => setTarefaEmEdicao(tarefa)}
                                  aria-label={`Editar tarefa: ${tarefa.descricao}`}
                                >
                                  Editar
                                </Button>
                                <Button
                                  variant="secondary"
                                  onClick={() => setTarefaClassificando(tarefa)}
                                  aria-label={`${
                                    tarefa.classificada ? 'Reavaliar' : 'Classificar'
                                  } risco da tarefa: ${tarefa.descricao}`}
                                >
                                  {tarefa.classificada ? 'Reavaliar risco' : 'Classificar'}
                                </Button>
                                <Button
                                  variant="secondary"
                                  onClick={() => setTarefaEpis(tarefa)}
                                  aria-label={`Vincular EPIs à tarefa: ${tarefa.descricao}`}
                                >
                                  EPIs{tarefa.episIds && tarefa.episIds.length > 0 ? ` (${tarefa.episIds.length})` : ''}
                                </Button>
                                <Button
                                  variant="secondary"
                                  onClick={() => setTarefaHistorico(tarefa)}
                                  aria-label={`Ver histórico da tarefa: ${tarefa.descricao}`}
                                >
                                  Histórico
                                </Button>
                              </>
                            )}
                          </div>
                        </td>
                      </TableRow>
                    ))}
                  </tbody>
                </Table>
              </>
            )
          }}
        </ResultadoConsulta>

        <TarefaFormDialog
          open={formularioAberto}
          aoFechar={() => setFormularioAberto(false)}
          aoSalvar={aoSalvarNovaTarefa}
        />

        <TarefaFormDialog
          open={!!tarefaEmEdicao}
          tarefa={tarefaEmEdicao}
          aoFechar={() => setTarefaEmEdicao(null)}
          aoSalvar={tarefa => {
            setMensagemSucesso(`Tarefa atualizada com sucesso: "${tarefa.descricao}".`)
            consultaTarefas.recarregar()
          }}
        />

        <ExigenciasTarefaDialog
          tarefa={tarefaExigencias}
          aoFechar={() => setTarefaExigencias(null)}
        />

        <ClassificarTarefaDialog
          tarefa={tarefaClassificando}
          aoFechar={() => setTarefaClassificando(null)}
          aoSalvar={aoSalvarClassificacao}
        />

        <HistoricoTarefaDialog
          tarefa={tarefaHistorico}
          aoFechar={() => setTarefaHistorico(null)}
        />

        <VincularEpisTarefaDialog
          tarefa={tarefaEpis}
          aoFechar={() => setTarefaEpis(null)}
          aoSalvar={aoSalvarEpis}
        />

        <Toast
          message={mensagemSucesso}
          onDismiss={() => setMensagemSucesso('')}
        />
      </div>
    </FeaturePage>
  )
}
