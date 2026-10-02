// Tela /areas-risco (RF05, UC03): abas de áreas de risco, com busca, filtro por grau, consulta,
// cadastro e edição, e de setores físicos (issue #174). O Gestor mantém os cadastros; o Supervisor
// consulta. Os setores são carregados aqui para que um setor novo apareça no formulário de área.

import { useState, useTransition, type FormEvent } from 'react'
import {
  Button,
  EmptyState,
  RiskAreaCard,
  SearchInput,
  Select,
  Tabs,
  Toast,
} from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useConsulta } from '../../shared/api/useConsulta'
import { useConsultaOffline } from '../../shared/offline/useConsultaOffline'
import { OfflineBanner } from '../../shared/offline/OfflineBanner'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { listarEpis } from '../epis/api'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import {
  descreverSetor,
  listarAreasRisco,
  listarSetores,
  niveisPerigo,
  rotuloNivelPerigo,
  tomDoNivelPerigo,
  type AreaRisco,
} from './api'
import { AreaRiscoDetalhesDialog } from './AreaRiscoDetalhesDialog'
import { AreaRiscoFormDialog } from './AreaRiscoFormDialog'
import { SetoresPainel } from './SetoresPainel'
import './areas-risco.css'

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
}

export function RiskAreasPage() {
  const sessao = useSessaoAtiva()
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'

  const [aba, setAba] = useState('areas')
  const [busca, setBusca] = useState('')
  const [filtroNivel, setFiltroNivel] = useState<string>('')
  const [, startTransition] = useTransition()

  const [areaDetalhando, setAreaDetalhando] = useState<AreaRisco | null>(null)
  const [formularioAberto, setFormularioAberto] = useState(false)
  const [areaEditando, setAreaEditando] = useState<AreaRisco | null>(null)
  const [mensagemSucesso, setMensagemSucesso] = useState('')

  const consultaAreas = useConsultaOffline('areas-risco', listarAreasRisco)
  const consultaSetores = useConsultaOffline('setores', listarSetores)
  const consultaEpis = useConsulta('epis', listarEpis)

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
    consultaAreas.recarregar()
  }

  function aoSalvarArea(areaSalva: AreaRisco, modo: 'criado' | 'atualizado') {
    setMensagemSucesso(
      modo === 'criado'
        ? `Área de risco ${areaSalva.codigo} cadastrada com sucesso.`
        : `Área de risco ${areaSalva.codigo} atualizada com sucesso.`
    )
    consultaAreas.recarregar()
  }

  function abrirEdicao(area: AreaRisco) {
    setAreaDetalhando(null)
    setAreaEditando(area)
    setFormularioAberto(true)
  }

  const setores = consultaSetores.dados ?? []
  const epis = consultaEpis.dados ?? []

  return (
    <FeaturePage
      title="Áreas de risco"
      description={
        gestor
          ? 'Consulte, cadastre e gerencie as áreas de risco, agentes mapeados e os EPIs obrigatórios para acesso (RF05, UC03).'
          : 'Consulte o mapa de riscos, agentes mapeados e as diretrizes de proteção para acesso aos setores (RF05, UC03).'
      }
    >
      <Tabs label="Conteúdo do mapa de riscos" value={aba} onValueChange={setAba} items={[
        {
          id: 'areas',
          label: 'Áreas de risco',
          content: (
          <div className="sp-painel">
            <OfflineBanner
              isOffline={consultaAreas.isOffline || consultaSetores.isOffline}
              sincronizadoEm={consultaAreas.sincronizadoEm}
            />
            <div className="sp-painel-topo">
              <form
                className="sp-busca-filtros"
                role="search"
                aria-label="Buscar e filtrar áreas de risco"
                onSubmit={aoFiltrarTexto}
                onReset={limparFiltros}
              >
                <SearchInput
                  label="Buscar área"
                  name="busca"
                  className="sp-busca-campo"
                  hiddenLabel={false}
                  placeholder="Código, nome ou demarcação"
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
                  disabled={consultaAreas.isOffline}
                  title={consultaAreas.isOffline ? 'Operação indisponível em modo offline' : undefined}
                  onClick={() => {
                    setAreaEditando(null)
                    setFormularioAberto(true)
                  }}
                >
                  Cadastrar área
                </Button>
              )}
            </div>

            <ResultadoConsulta
              consulta={consultaAreas}
              carregandoLabel="Carregando áreas de risco..."
              erroTitulo="Não foi possível carregar as áreas de risco."
              vazio={{
                title: 'Nenhuma área de risco cadastrada',
                description: 'As áreas de risco cadastradas aparecerão aqui.',
              }}
            >
              {areas => {
                const termo = normalizar(busca.trim())
                const filtradas = areas.filter(area => {
                  if (filtroNivel && area.nivelPerigo !== filtroNivel) return false
                  if (!termo) return true
                  const texto = normalizar(
                    `${area.codigo} ${area.nome} ${area.descricao ?? ''} ${area.limites ?? ''} ${area.agentesRisco.join(' ')}`
                  )
                  return texto.includes(termo)
                })

                if (filtradas.length === 0) {
                  return (
                    <EmptyState
                      title="Nenhuma área encontrada"
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
                      {filtradas.length === 1 ? 'área encontrada' : 'áreas encontradas'}
                    </p>
                    <div className="ds-risk-grid">
                      {filtradas.map(area => {
                        const setor =
                          area.setorId !== null
                            ? setores.find(s => s.id === area.setorId)
                            : null
                        const localizacao = setor
                          ? descreverSetor(setor)
                          : area.limites || 'Localização não especificada'
                        const hazards =
                          area.agentesRisco.length > 0
                            ? area.agentesRisco.join(', ')
                            : 'Nenhum perigo específico mapeado'
                        const ppe =
                          area.episObrigatorios.length > 0
                            ? area.episObrigatorios.map(e => e.nome).join('; ')
                            : 'Nenhum EPI obrigatório cadastrado'

                        return (
                          <RiskAreaCard
                            key={area.id}
                            title={`${area.codigo} - ${area.nome}`}
                            location={localizacao}
                            hazards={hazards}
                            ppe={ppe}
                            riskLabel={rotuloNivelPerigo[area.nivelPerigo]}
                            riskTone={tomDoNivelPerigo[area.nivelPerigo]}
                            onConsult={() => setAreaDetalhando(area)}
                          />
                        )
                      })}
                    </div>
                  </>
                )
              }}
            </ResultadoConsulta>

            <AreaRiscoDetalhesDialog
              area={areaDetalhando}
              setores={setores}
              podeEditar={gestor}
              aoFechar={() => setAreaDetalhando(null)}
              aoEditar={abrirEdicao}
            />

            <AreaRiscoFormDialog
              open={formularioAberto}
              area={areaEditando}
              setores={setores}
              epis={epis}
              aoFechar={() => {
                setFormularioAberto(false)
                setAreaEditando(null)
              }}
              aoSalvar={aoSalvarArea}
            />

            <Toast
              message={mensagemSucesso}
              onDismiss={() => setMensagemSucesso('')}
            />
          </div>
          ),
        },
        {
          id: 'setores',
          label: 'Setores físicos',
          content: (
            <SetoresPainel
              consulta={consultaSetores}
              areas={consultaAreas.dados}
              gestor={gestor}
              aoCadastrar={() => consultaSetores.recarregar()}
            />
          ),
        },
      ]} />
    </FeaturePage>
  )
}
