// Diálogo de detalhe de uma ocorrência (UC05/UC09, issues #128 e #100): consulta o servidor pelo id,
// para que o detalhe respeite o perfil, e mostra a data do fato separada da data do registro, a área,
// o colaborador envolvido e os dados próprios de acidente ou incidente. Em abas, o plano de ação. Ao
// Gestor oferece editar, enviar para triagem e arquivar (Aberta → Em triagem → Arquivada, sem exclusão).

import { useCallback, useState } from 'react'
import { Alert, Badge, Button, Dialog, Tabs } from '../../shared/components'
import { statusDoErro } from '../../shared/api/erros'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarDataHora } from '../estoque/formatos'
import {
  arquivarOcorrencia,
  avisarOcorrenciasAlteradas,
  buscarOcorrenciaPorId,
  descreverFalhaDeOcorrencia,
  enviarParaTriagem,
  rotuloStatusOcorrencia,
  type OcorrenciaResumo,
} from './api'
import { descreverCausaRaiz, descreverGravidade, rotuloTipoOcorrencia, textoOuPadrao, tomDoStatus } from './formatos'
import { PlanoAcaoPainel } from './PlanoAcaoPainel'

export type AbaDetalhe = 'dados' | 'plano'

export function OcorrenciaDetalheDialog({ idOcorrencia, aba, gestor, offline, aoMudarAba, aoEditar, aoFechar }: {
  idOcorrencia: number | null
  aba: AbaDetalhe
  gestor: boolean
  offline: boolean
  aoMudarAba: (aba: AbaDetalhe) => void
  aoEditar: (ocorrencia: OcorrenciaResumo) => void
  aoFechar: () => void
}) {
  return <Dialog open={idOcorrencia !== null} title={idOcorrencia !== null ? `Ocorrência #${idOcorrencia}` : 'Ocorrência'}
    description="Dados registrados no relato, situação atual e plano de ação." onClose={aoFechar} className="sp-dialog--largo">
    {idOcorrencia !== null && <Detalhe key={idOcorrencia} {...{ idOcorrencia, aba, gestor, offline, aoMudarAba, aoEditar, aoFechar }} />}
  </Dialog>
}

function Detalhe({ idOcorrencia, aba, gestor, offline, aoMudarAba, aoEditar, aoFechar }: {
  idOcorrencia: number
  aba: AbaDetalhe
  gestor: boolean
  offline: boolean
  aoMudarAba: (aba: AbaDetalhe) => void
  aoEditar: (ocorrencia: OcorrenciaResumo) => void
  aoFechar: () => void
}) {
  const [falha, setFalha] = useState<number | undefined>()
  const carregar = useCallback((signal: AbortSignal) => buscarOcorrenciaPorId(idOcorrencia, signal)
    .catch(erro => { setFalha(statusDoErro(erro)); throw erro }), [idOcorrencia, setFalha])
  const consulta = useConsulta(`ocorrencia-${idOcorrencia}`, carregar)
  // Mantém os últimos dados na tela enquanto o detalhe é reconsultado após uma alteração, para não
  // desmontar o plano de ação nem o aviso do que acabou de ser feito.
  const [ultima, setUltima] = useState<OcorrenciaResumo | null>(null)
  if (consulta.dados && consulta.dados !== ultima) setUltima(consulta.dados)
  const ocorrencia = consulta.dados ?? ultima
  const [aviso, setAviso] = useState('')

  function alterou(atualizada: OcorrenciaResumo, mensagem: string) {
    setUltima(atualizada)
    setAviso(mensagem)
    consulta.recarregar()
    avisarOcorrenciasAlteradas()
  }

  return <div className="sp-dialog-body">
    {consulta.erro && (falha === 404
      ? <Alert tone="warning" title="Ocorrência indisponível" live="polite">
        A ocorrência não existe ou não está entre os relatos que seu perfil pode consultar.
      </Alert>
      : <><Alert tone="danger" title="Não foi possível carregar a ocorrência" live="assertive">
        Verifique sua conexão e tente novamente.
      </Alert><div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div></>)}
    {consulta.carregando && !ocorrencia && <p role="status">Carregando ocorrência...</p>}
    {ocorrencia && <>
      {gestor && <AcoesDoGestor ocorrencia={ocorrencia} offline={offline} aoEditar={aoEditar} aoAlterar={alterou} />}
      {aviso && <Alert tone="success" title={aviso} live="polite">A situação foi gravada e já aparece na lista.</Alert>}
      <Tabs label={`Detalhe da ocorrência #${idOcorrencia}`} value={aba} onValueChange={valor => aoMudarAba(valor as AbaDetalhe)} items={[
        { id: 'dados', label: 'Dados do relato', content: <Dados ocorrencia={ocorrencia} /> },
        { id: 'plano', label: 'Plano de ação', content: <PlanoAcaoPainel ocorrencia={ocorrencia} gestor={gestor} offline={offline} ativo={aba === 'plano'} /> },
      ]} />
    </>}
    <div className="sp-dialog-actions"><Button variant="secondary" onClick={aoFechar}>Fechar</Button></div>
  </div>
}

function AcoesDoGestor({ ocorrencia, offline, aoEditar, aoAlterar }: {
  ocorrencia: OcorrenciaResumo
  offline: boolean
  aoEditar: (ocorrencia: OcorrenciaResumo) => void
  aoAlterar: (atualizada: OcorrenciaResumo, mensagem: string) => void
}) {
  const [confirmandoArquivo, setConfirmandoArquivo] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [falha, setFalha] = useState('')
  const status = ocorrencia.statusOcorrencia

  async function executar(tarefa: () => Promise<OcorrenciaResumo>, mensagem: string) {
    setFalha('')
    setEnviando(true)
    try {
      const atualizada = await tarefa()
      setConfirmandoArquivo(false)
      aoAlterar(atualizada, mensagem)
    } catch (erro) {
      setFalha(descreverFalhaDeOcorrencia(erro))
    } finally {
      setEnviando(false)
    }
  }

  if (status === 'ARQUIVADA') {
    return <Alert tone="info" title="Ocorrência arquivada">
      O acompanhamento foi encerrado. Os dados e o plano de ação ficam preservados; não há exclusão de registros.
    </Alert>
  }

  return <div className="sp-ocorrencia-gestao">
    <div className="sp-acao-botoes" role="group" aria-label="Ações do Gestor de Segurança">
      <Button variant="secondary" onClick={() => aoEditar(ocorrencia)} disabled={offline || enviando}>Editar dados</Button>
      {status === 'ABERTA' && <Button icon="check" loading={enviando} disabled={offline}
        onClick={() => executar(() => enviarParaTriagem(ocorrencia.idOcorrencia), 'Ocorrência enviada para triagem.')}>
        Enviar para triagem
      </Button>}
      {status === 'EM_TRIAGEM' && !confirmandoArquivo && <Button variant="danger" disabled={offline || enviando}
        onClick={() => setConfirmandoArquivo(true)}>Arquivar</Button>}
    </div>
    {offline && <p className="sp-texto-secundario">Alterações indisponíveis sem conexão.</p>}
    {confirmandoArquivo && <div className="sp-acao-operacao" role="group" aria-label="Confirmar arquivamento">
      <p>Arquivar encerra o acompanhamento desta ocorrência. O registro e o plano de ação são preservados, sem exclusão.</p>
      <div className="sp-dialog-actions">
        <Button variant="secondary" disabled={enviando} onClick={() => setConfirmandoArquivo(false)}>Cancelar</Button>
        <Button variant="danger" loading={enviando}
          onClick={() => executar(() => arquivarOcorrencia(ocorrencia.idOcorrencia), 'Ocorrência arquivada.')}>Confirmar arquivamento</Button>
      </div>
    </div>}
    {falha && <Alert tone="danger" title="Operação não realizada" live="assertive">{falha}</Alert>}
  </div>
}

function Dados({ ocorrencia }: { ocorrencia: OcorrenciaResumo }) {
  const acidente = ocorrencia.tipoOcorrencia === 'ACIDENTE'
  return <dl className="sp-detalhes">
    <div><dt>Tipo</dt><dd>{rotuloTipoOcorrencia[ocorrencia.tipoOcorrencia]}</dd></div>
    <div><dt>Situação</dt><dd><Badge tone={tomDoStatus(ocorrencia.statusOcorrencia)}>{rotuloStatusOcorrencia[ocorrencia.statusOcorrencia]}</Badge></dd></div>
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
