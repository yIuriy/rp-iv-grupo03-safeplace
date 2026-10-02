// Diálogo de detalhe de uma ocorrência (UC05/UC09, issue #128): consulta o servidor pelo id, para que
// o detalhe respeite o perfil, e mostra a data do fato separada da data do registro, a área, o
// colaborador envolvido e os dados próprios de acidente ou incidente.

import { useCallback, useState } from 'react'
import { Alert, Badge, Button, Dialog } from '../../shared/components'
import { statusDoErro } from '../../shared/api/erros'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarDataHora } from '../estoque/formatos'
import { buscarOcorrenciaPorId, rotuloStatusOcorrencia, type OcorrenciaResumo } from './api'
import { descreverCausaRaiz, descreverGravidade, rotuloTipoOcorrencia, textoOuPadrao } from './formatos'

export function OcorrenciaDetalheDialog({ idOcorrencia, aoFechar }: { idOcorrencia: number | null; aoFechar: () => void }) {
  return <Dialog open={idOcorrencia !== null} title={idOcorrencia !== null ? `Ocorrência #${idOcorrencia}` : 'Ocorrência'}
    description="Dados registrados no relato e situação atual do acompanhamento." onClose={aoFechar}>
    {idOcorrencia !== null && <Detalhe key={idOcorrencia} idOcorrencia={idOcorrencia} aoFechar={aoFechar} />}
  </Dialog>
}

function Detalhe({ idOcorrencia, aoFechar }: { idOcorrencia: number; aoFechar: () => void }) {
  const [falha, setFalha] = useState<number | undefined>()
  const carregar = useCallback((signal: AbortSignal) => buscarOcorrenciaPorId(idOcorrencia, signal)
    .catch(erro => { setFalha(statusDoErro(erro)); throw erro }), [idOcorrencia, setFalha])
  const consulta = useConsulta(`ocorrencia-${idOcorrencia}`, carregar)

  return <div className="sp-dialog-body">
    {consulta.erro && (falha === 404
      ? <Alert tone="warning" title="Ocorrência indisponível" live="polite">
        A ocorrência não existe ou não está entre os relatos que seu perfil pode consultar.
      </Alert>
      : <><Alert tone="danger" title="Não foi possível carregar a ocorrência" live="assertive">
        Verifique sua conexão e tente novamente.
      </Alert><div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div></>)}
    {consulta.carregando && <p role="status">Carregando ocorrência...</p>}
    {consulta.dados && <Dados ocorrencia={consulta.dados} />}
    <div className="sp-dialog-actions"><Button variant="secondary" onClick={aoFechar}>Fechar</Button></div>
  </div>
}

function Dados({ ocorrencia }: { ocorrencia: OcorrenciaResumo }) {
  const acidente = ocorrencia.tipoOcorrencia === 'ACIDENTE'
  return <dl className="sp-detalhes">
    <div><dt>Tipo</dt><dd>{rotuloTipoOcorrencia[ocorrencia.tipoOcorrencia]}</dd></div>
    <div><dt>Situação</dt><dd><Badge tone="info">{rotuloStatusOcorrencia[ocorrencia.statusOcorrencia]}</Badge></dd></div>
    <div><dt>Data do fato</dt><dd>{formatarDataHora(ocorrencia.dataOcorrencia)}</dd></div>
    <div><dt>Data do registro</dt><dd>{ocorrencia.dataRegistro ? formatarDataHora(ocorrencia.dataRegistro) : 'Não informada'}</dd></div>
    <div><dt>Área de risco</dt><dd>{textoOuPadrao(ocorrencia.areaNome, 'Não vinculada')}</dd></div>
    <div><dt>Colaborador envolvido</dt><dd>{textoOuPadrao(ocorrencia.colaboradorNome)}</dd></div>
    {ocorrencia.gestorNome && <div><dt>Gestor de Segurança</dt><dd>{ocorrencia.gestorNome}</dd></div>}
    {acidente && <>
      <div><dt>Protocolo CAT</dt><dd>{textoOuPadrao(ocorrencia.numeroProtocoloCAT, 'Ainda não consolidado')}</dd></div>
      <div><dt>Gravidade</dt><dd>{descreverGravidade(ocorrencia.gravidade)}</dd></div>
      <div><dt>Causa raiz</dt><dd>{descreverCausaRaiz(ocorrencia.causaRaiz)}</dd></div>
      <div><dt>Tipo de lesão</dt><dd>{textoOuPadrao(ocorrencia.tipo)}</dd></div>
      <div><dt>Dano</dt><dd>{textoOuPadrao(ocorrencia.dano)}</dd></div>
      <div><dt>Destino</dt><dd>{textoOuPadrao(ocorrencia.destino)}</dd></div>
    </>}
    {!acidente && <>
      <div><dt>Situação de risco</dt><dd>{textoOuPadrao(ocorrencia.situacaoRisco, 'Não informada')}</dd></div>
      <div><dt>Potencial de dano</dt><dd>{textoOuPadrao(ocorrencia.potencialDano)}</dd></div>
    </>}
    <div className="sp-detalhes-largo"><dt>Descrição</dt><dd>{ocorrencia.descricao}</dd></div>
  </dl>
}
