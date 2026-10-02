// Tela /ocorrencias (UC05/UC09): consulta por critérios no servidor (período da data do fato, setor e
// colaborador envolvido, issue #128) e refinamento local por texto, tipo e situação; busca de acidente
// pelo protocolo CAT; detalhe de cada ocorrência. Lista com cópia offline por critério (RNF08).

import { useCallback, useState } from 'react'
import { Badge, Button, EmptyState, Table, TableRow } from '../../shared/components'
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
  listarOcorrencias,
  rotuloStatusOcorrencia,
  temCriterios,
  type OcorrenciaResumo,
  type StatusOcorrencia,
} from './api'
import { ConsultaOcorrenciasForm } from './ConsultaOcorrenciasForm'
import { SEM_CRITERIOS, type CriteriosLocais } from './criteriosConsulta'
import { rotuloTipoOcorrencia } from './formatos'
import { OcorrenciaDetalheDialog } from './OcorrenciaDetalheDialog'
import { ProtocoloBusca } from './ProtocoloBusca'
import './ocorrencias.css'

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLocaleLowerCase('pt-BR')
}

function refinar(itens: OcorrenciaResumo[], { texto, tipo, status }: CriteriosLocais): OcorrenciaResumo[] {
  const termo = normalizar(texto)
  return itens.filter(item => {
    if (tipo && item.tipoOcorrencia !== tipo) return false
    if (status && item.statusOcorrencia !== status) return false
    if (!termo) return true
    return normalizar(`${item.idOcorrencia} ${item.descricao} ${item.numeroProtocoloCAT ?? ''} ${item.colaboradorNome ?? ''} ${item.areaNome ?? ''}`)
      .includes(termo)
  })
}

const carregarColaboradores = (signal: AbortSignal) => listarColaboradores({}, signal)

export function OccurrencesPage() {
  const sessao = useSessaoAtiva()
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'

  const [criterios, setCriterios] = useState(SEM_CRITERIOS)
  const [versaoFormulario, setVersaoFormulario] = useState(0)
  const [detalhando, setDetalhando] = useState<number | null>(null)

  const filtro = criterios.filtro
  const carregar = useCallback((signal: AbortSignal) => listarOcorrencias(filtro, signal), [filtro])
  const consulta = useConsultaOffline(`ocorrencias:${JSON.stringify(filtro)}`, carregar)
  const setores = useConsulta('setores-consulta-ocorrencias', listarSetores)
  const colaboradores = useConsulta('colaboradores-consulta-ocorrencias', carregarColaboradores)
  const comCriterios = temCriterios(filtro)

  function limpar() {
    setCriterios(SEM_CRITERIOS)
    setVersaoFormulario(versao => versao + 1)
  }

  return (
    <FeaturePage
      title="Ocorrências"
      description={
        gestor
          ? 'Consulte, acompanhe e faça a triagem dos acidentes e incidentes de trabalho (UC05).'
          : 'Consulte os relatos de acidentes e incidentes que você registrou (UC09).'
      }
    >
      <div className="sp-painel">
        <OfflineBanner isOffline={consulta.isOffline} sincronizadoEm={consulta.sincronizadoEm} />

        <div className="sp-consulta-cabecalho">
          <ProtocoloBusca aoEncontrar={setDetalhando} desabilitado={consulta.isOffline} />
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
                      <th scope="col">ID / Tipo</th>
                      <th scope="col">Data do fato</th>
                      <th scope="col">Situação</th>
                      <th scope="col">Colaborador</th>
                      <th scope="col">Área de risco</th>
                      <th scope="col">Protocolo CAT</th>
                      <th scope="col">Descrição</th>
                      <th scope="col">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtradas.map(oc => (
                      <TableRow key={oc.idOcorrencia}>
                        <td>
                          <strong>#{oc.idOcorrencia}</strong> ·{' '}
                          <Badge tone={oc.tipoOcorrencia === 'ACIDENTE' ? 'danger' : 'warning'}>
                            {rotuloTipoOcorrencia[oc.tipoOcorrencia]}
                          </Badge>
                        </td>
                        <td>{formatarDataHora(oc.dataOcorrencia)}</td>
                        <td>
                          <Badge tone={obterTomStatus(oc.statusOcorrencia)}>
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
                        <td className="sp-descricao-ocorrencia">{oc.descricao}</td>
                        <td>
                          <Button variant="secondary" onClick={() => setDetalhando(oc.idOcorrencia)}
                            aria-label={`Ver detalhes da ocorrência ${oc.idOcorrencia}`}>
                            Ver detalhes
                          </Button>
                        </td>
                      </TableRow>
                    ))}
                  </tbody>
                </Table>
              </>
            )
          }}
        </ResultadoConsulta>

        <OcorrenciaDetalheDialog idOcorrencia={detalhando} aoFechar={() => setDetalhando(null)} />
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
