// Tela /ocorrencias (UC05/UC09): relato de acidente ou incidente com confirmação do protocolo,
// consulta por critérios no servidor (período da data do fato, setor e colaborador envolvido, issue
// #128) e refinamento local por texto, tipo e situação; busca de acidente pelo protocolo CAT; detalhe
// com triagem, edição, arquivamento e plano de ação (issue #100). A ocorrência aberta e a aba ficam
// na URL, para que recarregar a página volte ao mesmo ponto. Lista com cópia offline (RNF08).

import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Badge, Button, EmptyState, Table, TableRow, Toast } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useConsultaOffline } from '../../shared/offline/useConsultaOffline'
import { OfflineBanner } from '../../shared/offline/OfflineBanner'
import { listarSetores } from '../areas-risco/api'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { formatarDataHora } from '../estoque/formatos'
import { listarColaboradores } from '../usuarios/api'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import {
  avisarOcorrenciasAlteradas,
  EVENTO_OCORRENCIAS_ALTERADAS,
  listarOcorrencias,
  rotuloStatusOcorrencia,
  temCriterios,
  type OcorrenciaResumo,
} from './api'
import { ConfirmacaoRelatoDialog } from './ConfirmacaoRelatoDialog'
import { ConsultaOcorrenciasForm } from './ConsultaOcorrenciasForm'
import { SEM_CRITERIOS, type CriteriosLocais } from './criteriosConsulta'
import { rotuloTipoOcorrencia, tomDoStatus } from './formatos'
import { NovosRelatosAviso } from './NovosRelatosAviso'
import { OcorrenciaDetalheDialog, type AbaDetalhe } from './OcorrenciaDetalheDialog'
import { ProtocoloBusca } from './ProtocoloBusca'
import { RelatoFormDialog } from './RelatoFormDialog'
import { useNovosRelatos } from './useNovosRelatos'
import './ocorrencias.css'

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLocaleLowerCase('pt-BR')
}

function refinar(itens: OcorrenciaResumo[], { texto, status }: CriteriosLocais): OcorrenciaResumo[] {
  const termo = normalizar(texto)
  return itens.filter(item => {
    if (status && item.statusOcorrencia !== status) return false
    if (!termo) return true
    return normalizar(`${item.idOcorrencia} ${item.descricao} ${item.numeroProtocoloCAT ?? ''} ${item.colaboradorNome ?? ''} ${item.areaNome ?? ''}`)
      .includes(termo)
  })
}

const carregarColaboradores = (signal: AbortSignal) => listarColaboradores({}, signal)

/** Lê `?ocorrencia=` e `?aba=` da URL; id inválido equivale a nenhum detalhe aberto. */
function lerDetalheDaUrl(params: URLSearchParams): { id: number | null; aba: AbaDetalhe } {
  const id = Number(params.get('ocorrencia'))
  return { id: Number.isInteger(id) && id > 0 ? id : null, aba: params.get('aba') === 'plano' ? 'plano' : 'dados' }
}

export function OccurrencesPage() {
  const sessao = useSessaoAtiva()
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'
  const [params, setParams] = useSearchParams()
  const detalhe = lerDetalheDaUrl(params)

  const [criterios, setCriterios] = useState(SEM_CRITERIOS)
  const [versaoFormulario, setVersaoFormulario] = useState(0)
  const [relatando, setRelatando] = useState(false)
  const [editando, setEditando] = useState<OcorrenciaResumo | null>(null)
  const [registrada, setRegistrada] = useState<OcorrenciaResumo | null>(null)
  const [aviso, setAviso] = useState('')

  const filtro = criterios.filtro
  const carregar = useCallback((signal: AbortSignal) => listarOcorrencias(filtro, signal), [filtro])
  const consulta = useConsultaOffline(`ocorrencias:${JSON.stringify(filtro)}`, carregar)
  const setores = useConsulta('setores-consulta-ocorrencias', listarSetores)
  const colaboradores = useConsulta('colaboradores-consulta-ocorrencias', carregarColaboradores)
  const novosRelatos = useNovosRelatos(gestor)
  const comCriterios = temCriterios(filtro)
  const recarregarLista = consulta.recarregar

  useEffect(() => {
    window.addEventListener(EVENTO_OCORRENCIAS_ALTERADAS, recarregarLista)
    return () => window.removeEventListener(EVENTO_OCORRENCIAS_ALTERADAS, recarregarLista)
  }, [recarregarLista])

  const abrirDetalhe = useCallback((id: number | null, aba: AbaDetalhe = 'dados') => {
    setParams(atuais => {
      const proximos = new URLSearchParams(atuais)
      if (id === null) {
        proximos.delete('ocorrencia')
        proximos.delete('aba')
      } else {
        proximos.set('ocorrencia', String(id))
        if (aba === 'plano') proximos.set('aba', 'plano')
        else proximos.delete('aba')
      }
      return proximos
    })
  }, [setParams])

  function limpar() {
    setCriterios(SEM_CRITERIOS)
    setVersaoFormulario(versao => versao + 1)
  }

  function relatoSalvo(ocorrencia: OcorrenciaResumo) {
    setRelatando(false)
    setRegistrada(ocorrencia)
    avisarOcorrenciasAlteradas()
  }

  function edicaoSalva(ocorrencia: OcorrenciaResumo) {
    setEditando(null)
    setAviso(`Ocorrência #${ocorrencia.idOcorrencia} atualizada.`)
    avisarOcorrenciasAlteradas()
    abrirDetalhe(ocorrencia.idOcorrencia)
  }

  return (
    <FeaturePage
      title="Ocorrências"
      description={
        gestor
          ? 'Registre, consulte, acompanhe e faça a triagem dos acidentes e incidentes de trabalho.'
          : 'Relate acidentes e incidentes e acompanhe os relatos que você registrou.'
      }
    >
      <div className="sp-painel">
        <OfflineBanner isOffline={consulta.isOffline} sincronizadoEm={consulta.sincronizadoEm} />

        {gestor && <NovosRelatosAviso relatos={novosRelatos.relatos} aoAbrir={id => abrirDetalhe(id)} />}

        <div className="sp-consulta-cabecalho">
          <ProtocoloBusca aoEncontrar={id => abrirDetalhe(id)} desabilitado={consulta.isOffline} />
          <Button
            variant="primary"
            icon="plus"
            disabled={consulta.isOffline}
            title={consulta.isOffline ? 'Registro de novos relatos indisponível em modo offline' : undefined}
            onClick={() => setRelatando(true)}
          >
            Relatar ocorrência
          </Button>
        </div>
        {consulta.isOffline && <p className="sp-texto-secundario" role="note">
          Sem conexão, o envio de relatos fica indisponível; a lista mostra a última cópia sincronizada.
        </p>}

        <ConsultaOcorrenciasForm key={versaoFormulario} setores={setores} colaboradores={colaboradores}
          aoConsultar={setCriterios} aoLimpar={limpar} />

        <ResultadoConsulta
          consulta={consulta}
          carregandoLabel="Carregando ocorrências..."
          erroTitulo="Não foi possível carregar as ocorrências."
          vazio={comCriterios
            ? { title: 'Nenhuma ocorrência atende aos critérios', description: 'Ajuste o período, o setor ou o colaborador, ou limpe a consulta.' }
            : { title: 'Nenhuma ocorrência registrada', description: 'Os acidentes e incidentes registrados aparecerão aqui.' }}
        >
          {itens => {
            const filtradas = refinar(itens, criterios.locais)

            if (filtradas.length === 0) {
              return (
                <EmptyState
                  title="Nenhuma ocorrência encontrada"
                  description="Ajuste o termo de busca, o tipo ou a situação para localizar registros."
                  actionLabel="Limpar consulta"
                  onAction={limpar}
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
                      <th scope="col">ID</th>
                      <th scope="col">Tipo</th>
                      <th scope="col">Data do fato</th>
                      <th scope="col">Situação</th>
                      <th scope="col">Colaborador</th>
                      <th scope="col">Área de risco</th>
                      <th scope="col">Protocolo CAT</th>
                      <th scope="col">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtradas.map(oc => (
                      <TableRow key={oc.idOcorrencia}>
                        <td><strong>{oc.idOcorrencia}</strong></td>
                        <td>
                          <Badge tone={oc.tipoOcorrencia === 'ACIDENTE' ? 'danger' : 'warning'}>
                            {rotuloTipoOcorrencia[oc.tipoOcorrencia]}
                          </Badge>
                        </td>
                        <td>{formatarDataHora(oc.dataOcorrencia)}</td>
                        <td>
                          <Badge tone={tomDoStatus(oc.statusOcorrencia)}>
                            {rotuloStatusOcorrencia[oc.statusOcorrencia]}
                          </Badge>
                        </td>
                        <td>{oc.colaboradorNome || 'Não informado'}</td>
                        <td>{oc.areaNome || 'Não vinculada'}</td>
                        <td>
                          {oc.numeroProtocoloCAT ? (
                            <code>{oc.numeroProtocoloCAT}</code>
                          ) : (
                            <span className="sp-texto-secundario">Sem protocolo</span>
                          )}
                        </td>
                        <td>
                          <div className="sp-acoes-linha">
                            <Button variant="secondary" onClick={() => abrirDetalhe(oc.idOcorrencia)}
                              aria-label={`Ver detalhes da ocorrência ${oc.idOcorrencia}`}>
                              Ver detalhes
                            </Button>
                            <Button variant="quiet" onClick={() => abrirDetalhe(oc.idOcorrencia, 'plano')}
                              aria-label={`Ver plano de ação da ocorrência ${oc.idOcorrencia}`}>
                              Plano de ação
                            </Button>
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

        <Toast message={aviso} onDismiss={() => setAviso('')} />

        <OcorrenciaDetalheDialog idOcorrencia={editando ? null : detalhe.id} aba={detalhe.aba} gestor={gestor}
          offline={consulta.isOffline}
          aoMudarAba={aba => abrirDetalhe(detalhe.id, aba)}
          aoEditar={ocorrencia => setEditando(ocorrencia)}
          aoFechar={() => abrirDetalhe(null)} />

        <RelatoFormDialog aberto={relatando} aoFechar={() => setRelatando(false)} aoSalvar={relatoSalvo} />
        <RelatoFormDialog aberto={editando !== null} ocorrencia={editando}
          aoFechar={() => setEditando(null)} aoSalvar={edicaoSalva} />

        <ConfirmacaoRelatoDialog ocorrencia={registrada} aoFechar={() => setRegistrada(null)}
          aoAcompanhar={id => { setRegistrada(null); abrirDetalhe(id) }} />
      </div>
    </FeaturePage>
  )
}
