// Alertas do UC02 calculados pela API na data da consulta: certificações vencidas que necessitam de
// renovação (cenário alternativo I), certificações que vencem dentro da antecedência de alerta
// (regra 1, 30 dias), colaboradores sem histórico de certificações (cenário alternativo III) e
// treinamentos com validade vencidos ou próximos do vencimento (RF10, #192).

import { Alert, Badge, Button, Table, TableRow } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarData } from '../usuarios/formatos'
import { consultarAlertas, type AlertaCertificacao, type AlertaTreinamento, type PendenciaColaborador } from './api'
import { plural, rotuloDaSituacao, rotuloDaSituacaoTreinamento, tomDaSituacao, tomDaSituacaoTreinamento } from './formatos'

export function AlertasPainel({ aoConsultar }: { aoConsultar: (colaboradorId: number) => void }) {
  const consulta = useConsulta('capacitacoes-alertas', consultarAlertas)

  if (consulta.erro) {
    return <div className="sp-painel">
      <Alert tone="danger" live="assertive" title="Não foi possível carregar os alertas de capacitação.">
        Verifique sua conexão ou tente novamente em alguns instantes.
      </Alert>
      <div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div>
    </div>
  }
  if (consulta.dados === null) return <p role="status">Carregando alertas...</p>

  const alertas = consulta.dados
  const total = alertas.vencidas.length + alertas.proximasDoVencimento.length + alertas.pendencias.length
    + alertas.treinamentosVencidos.length + alertas.treinamentosProximosDoVencimento.length
  return <div className="sp-painel">
    <p role="status" className="sp-texto-secundario">
      Situação calculada em {formatarData(alertas.dataReferencia)}. Alertas de vencimento com {alertas.diasAntecedenciaAlerta} dias
      de antecedência. {total === 0 ? 'Nenhum alerta no momento.' : ''}
    </p>
    <SecaoCertificacoes titulo="Certificações vencidas" alertas={alertas.vencidas} aoConsultar={aoConsultar}
      descricao="Necessitam de renovação. A certificação vence na própria data de vencimento."
      vazio="Nenhuma certificação vencida." />
    <SecaoCertificacoes titulo={`Vencem em até ${alertas.diasAntecedenciaAlerta} dias`}
      alertas={alertas.proximasDoVencimento} aoConsultar={aoConsultar}
      descricao="Da que vence primeiro para a última." vazio="Nenhuma certificação próxima do vencimento." />
    <SecaoTreinamentos titulo="Treinamentos vencidos" alertas={alertas.treinamentosVencidos} aoConsultar={aoConsultar}
      descricao="Só vencem os treinamentos cadastrados com validade. Treinamento vencido não atende a exigência da tarefa."
      vazio="Nenhum treinamento vencido." />
    <SecaoTreinamentos titulo={`Treinamentos que vencem em até ${alertas.diasAntecedenciaAlerta} dias`}
      alertas={alertas.treinamentosProximosDoVencimento} aoConsultar={aoConsultar}
      descricao="Do que vence primeiro para o último." vazio="Nenhum treinamento próximo do vencimento." />
    <SecaoPendencias pendencias={alertas.pendencias} aoConsultar={aoConsultar} />
  </div>
}

function SecaoCertificacoes({ titulo, descricao, vazio, alertas, aoConsultar }: {
  titulo: string
  descricao: string
  vazio: string
  alertas: AlertaCertificacao[]
  aoConsultar: (colaboradorId: number) => void
}) {
  return <section className="sp-secao" aria-label={titulo}>
    <div>
      <h2 className="sp-type-heading-sm">{titulo} <span className="sp-texto-secundario">({alertas.length})</span></h2>
      <p className="sp-texto-secundario">{descricao}</p>
    </div>
    {alertas.length === 0 ? <p>{vazio}</p> : <Table caption={titulo}>
      <thead><tr>
        <th scope="col">Colaborador</th><th scope="col">Certificação</th><th scope="col">Vencimento</th>
        <th scope="col">Situação</th><th scope="col">Ações</th>
      </tr></thead>
      <tbody>{alertas.map(alerta => <TableRow key={alerta.certificacaoId}
        className={alerta.status === 'VENCIDA' ? 'sp-linha-critica' : undefined}>
        <td><strong>{alerta.colaboradorNome}</strong></td>
        <td>{alerta.nome}{alerta.dataEmissao && <><br />
          <span className="sp-texto-secundario">Emitida em {formatarData(alerta.dataEmissao)}</span></>}</td>
        <td>{formatarData(alerta.dataVencimento)}</td>
        <td><Badge tone={tomDaSituacao[alerta.status]}>{rotuloDaSituacao[alerta.status]}</Badge></td>
        <td><Button variant="secondary" onClick={() => aoConsultar(alerta.colaboradorId)}
          aria-label={`Consultar capacitações de ${alerta.colaboradorNome} (${alerta.nome})`}>Consultar</Button></td>
      </TableRow>)}</tbody>
    </Table>}
  </section>
}

function SecaoTreinamentos({ titulo, descricao, vazio, alertas, aoConsultar }: {
  titulo: string
  descricao: string
  vazio: string
  alertas: AlertaTreinamento[]
  aoConsultar: (colaboradorId: number) => void
}) {
  return <section className="sp-secao" aria-label={titulo}>
    <div>
      <h2 className="sp-type-heading-sm">{titulo} <span className="sp-texto-secundario">({alertas.length})</span></h2>
      <p className="sp-texto-secundario">{descricao}</p>
    </div>
    {alertas.length === 0 ? <p>{vazio}</p> : <Table caption={titulo}>
      <thead><tr>
        <th scope="col">Colaborador</th><th scope="col">Treinamento</th><th scope="col">Vencimento</th>
        <th scope="col">Situação</th><th scope="col">Ações</th>
      </tr></thead>
      <tbody>{alertas.map(alerta => <TableRow key={`${alerta.treinamentoId}-${alerta.colaboradorId}`}
        className={alerta.status === 'VENCIDO' ? 'sp-linha-critica' : undefined}>
        <td><strong>{alerta.colaboradorNome}</strong></td>
        <td>{alerta.nome}<br /><span className="sp-texto-secundario">Realizado em {formatarData(alerta.dataRealizacao)}</span></td>
        <td>{formatarData(alerta.dataVencimento)}</td>
        <td><Badge tone={tomDaSituacaoTreinamento[alerta.status]}>{rotuloDaSituacaoTreinamento[alerta.status]}</Badge></td>
        <td><Button variant="secondary" onClick={() => aoConsultar(alerta.colaboradorId)}
          aria-label={`Consultar capacitações de ${alerta.colaboradorNome} (${alerta.nome})`}>Consultar</Button></td>
      </TableRow>)}</tbody>
    </Table>}
  </section>
}

function SecaoPendencias({ pendencias, aoConsultar }: {
  pendencias: PendenciaColaborador[]
  aoConsultar: (colaboradorId: number) => void
}) {
  const titulo = 'Sem certificações cadastradas'
  return <section className="sp-secao" aria-label={titulo}>
    <div>
      <h2 className="sp-type-heading-sm">{titulo} <span className="sp-texto-secundario">({pendencias.length})</span></h2>
      <p className="sp-texto-secundario">
        A ausência de histórico indica pendência da trilha inicial de integração e de treinamentos admissionais.
      </p>
    </div>
    {pendencias.length === 0 ? <p>Todos os colaboradores têm certificações cadastradas.</p>
      : <ul className="sp-lista-pendencias">{pendencias.map(pendencia => <li key={pendencia.colaboradorId}>
        <div>
          <strong>{pendencia.colaboradorNome}</strong>
          <p className="sp-texto-secundario">{pendencia.mensagens.join('; ') || 'Pendência de capacitação.'}</p>
        </div>
        <Button variant="secondary" onClick={() => aoConsultar(pendencia.colaboradorId)}
          aria-label={`Consultar capacitações de ${pendencia.colaboradorNome}`}>Consultar</Button>
      </li>)}</ul>}
    <p className="sp-texto-secundario">{plural(pendencias.length, 'colaborador', 'colaboradores')} com pendência de integração.</p>
  </section>
}
