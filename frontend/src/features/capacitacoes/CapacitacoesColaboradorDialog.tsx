// Detalhes das capacitações de um colaborador (UC02, passos 4 a 7; US10, critério 1): certificações
// com emissão, vencimento e situação, treinamentos com conteúdo, instrutor, realização e carga
// horária, pendências (cenários I e III) e a consulta de aptidão para a tarefa pretendida.

import { useCallback } from 'react'
import { isAxiosError } from 'axios'
import { Alert, Badge, Button, Dialog, Table, TableRow } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarData } from '../../shared/formatos'
import { consultarColaborador, descreverFalha, type CapacitacoesColaborador, type Certificacao, type Treinamento } from './api'
import { AptidaoTarefa } from './AptidaoTarefa'
import {
  plural, rotuloDaSituacao, rotuloDaSituacaoTreinamento, situacaoDoResumo, textoSemCertificacoes, tomDaSituacao,
  tomDaSituacaoTreinamento,
} from './formatos'

export function CapacitacoesColaboradorDialog({ colaboradorId, aoFechar }: {
  colaboradorId: number | null
  aoFechar: () => void
}) {
  return <Dialog open={colaboradorId !== null} title="Capacitações do colaborador" className="sp-dialog--largo"
    description="Certificações, treinamentos, pendências e aptidão para tarefas." onClose={aoFechar}>
    {colaboradorId !== null && <Conteudo key={colaboradorId} colaboradorId={colaboradorId} aoFechar={aoFechar} />}
  </Dialog>
}

function Conteudo({ colaboradorId, aoFechar }: { colaboradorId: number; aoFechar: () => void }) {
  const carregar = useCallback(async (signal: AbortSignal) => {
    try {
      return { detalhe: await consultarColaborador(colaboradorId, signal), falha: null }
    } catch (erro) {
      if (isAxiosError(erro) && erro.code === 'ERR_CANCELED') throw erro
      return { detalhe: null, falha: descreverFalha(erro, 'Colaborador não encontrado. Ele pode ter sido removido.') }
    }
  }, [colaboradorId])
  const consulta = useConsulta(`capacitacoes-colaborador:${colaboradorId}`, carregar)

  const fechar = <div className="sp-dialog-actions"><Button variant="secondary" onClick={aoFechar}>Fechar</Button></div>
  if (consulta.dados === null && !consulta.erro) return <div className="sp-dialog-body"><p role="status">Carregando capacitações...</p></div>
  if (consulta.erro || consulta.dados?.detalhe === null) {
    return <div className="sp-dialog-body">
      <Alert tone="danger" live="assertive" title="Não foi possível consultar as capacitações.">
        {consulta.dados?.falha ?? 'Verifique sua conexão ou tente novamente em alguns instantes.'}
      </Alert>
      <div className="sp-dialog-actions">
        <Button variant="quiet" onClick={aoFechar}>Fechar</Button>
        <Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button>
      </div>
    </div>
  }
  const detalhe = consulta.dados!.detalhe!
  return <div className="sp-dialog-body">
    <Resumo detalhe={detalhe} />
    <Certificacoes certificacoes={detalhe.certificacoes} />
    <Treinamentos treinamentos={detalhe.treinamentos} />
    <AptidaoTarefa colaboradorId={detalhe.colaboradorId} colaboradorNome={detalhe.colaboradorNome} />
    {fechar}
  </div>
}

function Resumo({ detalhe }: { detalhe: CapacitacoesColaborador }) {
  const situacao = situacaoDoResumo(detalhe)
  const vencidas = detalhe.certificacoesVencidas.length > 0
  return <>
    <dl className="sp-detalhes">
      <div><dt>Colaborador</dt><dd>{detalhe.colaboradorNome}</dd></div>
      <div><dt>Situação das certificações</dt><dd><Badge tone={situacao.tom}>{situacao.rotulo}</Badge></dd></div>
      <div><dt>Registros</dt><dd>{plural(detalhe.certificacoes.length, 'certificação', 'certificações')}, {plural(detalhe.treinamentos.length, 'treinamento', 'treinamentos')}</dd></div>
    </dl>
    {detalhe.pendencias.length > 0 && <Alert tone={vencidas ? 'danger' : 'warning'}
      title={vencidas ? 'Certificações necessitam de renovação' : 'Pendências de capacitação'}>
      <ul className="sp-pendencias">{detalhe.pendencias.map(mensagem => <li key={mensagem}>{mensagem}</li>)}</ul>
    </Alert>}
  </>
}

function Certificacoes({ certificacoes }: { certificacoes: Certificacao[] }) {
  return <section className="sp-secao" aria-label="Certificações">
    <h3 className="sp-type-heading-sm">Certificações</h3>
    {certificacoes.length === 0
      ? <p>{textoSemCertificacoes}. Não há histórico de certificações para este colaborador.</p>
      : <Table caption="Certificações do colaborador">
        <thead><tr>
          <th scope="col">Certificação</th><th scope="col">Emissão</th><th scope="col">Vencimento</th><th scope="col">Situação</th>
        </tr></thead>
        <tbody>{certificacoes.map(certificacao => <TableRow key={certificacao.id}
          className={certificacao.status === 'VENCIDA' ? 'sp-linha-critica' : undefined}>
          <td><strong>{certificacao.nome}</strong></td>
          <td>{formatarData(certificacao.dataEmissao)}</td>
          <td>{formatarData(certificacao.dataVencimento)}</td>
          <td>
            <Badge tone={tomDaSituacao[certificacao.status]}>{rotuloDaSituacao[certificacao.status]}</Badge>
            {certificacao.status === 'VENCIDA' && <><br /><span className="sp-texto-secundario">Necessita de renovação</span></>}
          </td>
        </TableRow>)}</tbody>
      </Table>}
  </section>
}

function Treinamentos({ treinamentos }: { treinamentos: Treinamento[] }) {
  return <section className="sp-secao" aria-label="Treinamentos">
    <h3 className="sp-type-heading-sm">Treinamentos</h3>
    {treinamentos.length === 0
      ? <p>Nenhum treinamento registrado para este colaborador.</p>
      : <Table caption="Treinamentos do colaborador">
        <thead><tr>
          <th scope="col">Treinamento</th><th scope="col">Instrutor</th><th scope="col">Realização</th>
          <th scope="col">Carga horária</th><th scope="col">Vencimento</th><th scope="col">Situação</th>
        </tr></thead>
        <tbody>{treinamentos.map(treinamento => <TableRow key={treinamento.id}>
          <td><strong>{treinamento.nome}</strong>
            {treinamento.conteudo && <><br /><span className="sp-texto-secundario">{treinamento.conteudo}</span></>}</td>
          <td>{treinamento.instrutor ?? 'Não informado'}</td>
          <td>{formatarData(treinamento.dataRealizacao)}</td>
          <td>{treinamento.cargaHoraria === null ? 'Não informada' : `${treinamento.cargaHoraria} h`}</td>
          <td>{treinamento.dataVencimento ? formatarData(treinamento.dataVencimento) : 'Não vence'}</td>
          <td>
            <Badge tone={tomDaSituacaoTreinamento[treinamento.status]}>{rotuloDaSituacaoTreinamento[treinamento.status]}</Badge>
            {treinamento.status === 'VENCIDO' && <><br /><span className="sp-texto-secundario">Não atende a exigência da tarefa</span></>}
          </td>
        </TableRow>)}</tbody>
      </Table>}
    <p className="sp-texto-secundario">
      Só vencem os treinamentos cadastrados com validade. Os obrigatórios dependem da tarefa e aparecem na consulta de aptidão abaixo.
    </p>
  </section>
}
