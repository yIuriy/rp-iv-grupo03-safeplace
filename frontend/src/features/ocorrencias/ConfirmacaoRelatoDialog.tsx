// Confirmação do relato registrado (UC09, passo 11): protocolo de acompanhamento em destaque, data do
// fato separada da data do registro e aviso de que o Gestor foi notificado. Pela decisão DS09 (seção
// 2.1), só o acidente recebe o protocolo CAT; o incidente é acompanhado pelo número do registro.

import { Alert, Button, Dialog } from '../../shared/components'
import { formatarDataHora } from '../estoque/formatos'
import type { OcorrenciaResumo } from './api'
import { rotuloTipoOcorrencia } from './formatos'

export function ConfirmacaoRelatoDialog({ ocorrencia, aoAcompanhar, aoFechar }: {
  ocorrencia: OcorrenciaResumo | null
  aoAcompanhar: (idOcorrencia: number) => void
  aoFechar: () => void
}) {
  return <Dialog open={ocorrencia !== null} onClose={aoFechar} title="Relato registrado"
    description={ocorrencia ? `${rotuloTipoOcorrencia[ocorrencia.tipoOcorrencia]} registrado com sucesso. Guarde ${ocorrencia.numeroProtocoloCAT ? 'o protocolo' : 'o número do registro'} para acompanhar o relato.` : undefined}>
    {ocorrencia && <div className="sp-dialog-body">
      <div className="sp-protocolo-destaque" role="status" aria-live="polite">
        <span className="sp-protocolo-rotulo">
          {ocorrencia.numeroProtocoloCAT ? 'Protocolo de acompanhamento (CAT)' : 'Número de acompanhamento do registro'}
        </span>
        <strong className="sp-protocolo-numero">{ocorrencia.numeroProtocoloCAT ?? `#${ocorrencia.idOcorrencia}`}</strong>
        {!ocorrencia.numeroProtocoloCAT && <span className="sp-texto-secundario">
          Incidentes não recebem protocolo CAT; use este número para localizar o relato.
        </span>}
      </div>
      <dl className="sp-detalhes">
        <div><dt>Data do fato</dt><dd>{formatarDataHora(ocorrencia.dataOcorrencia)}</dd></div>
        <div><dt>Data do registro</dt><dd>{ocorrencia.dataRegistro ? formatarDataHora(ocorrencia.dataRegistro) : 'Não informada'}</dd></div>
        <div><dt>Área de risco</dt><dd>{ocorrencia.areaNome ?? 'Não vinculada'}</dd></div>
        <div><dt>Colaborador envolvido</dt><dd>{ocorrencia.colaboradorNome ?? 'Não informado'}</dd></div>
      </dl>
      <Alert tone="info" title="Gestor de Segurança notificado">
        O relato entrou na fila de triagem e aparece como novo relato para o Gestor de Segurança.
      </Alert>
      <div className="sp-dialog-actions">
        <Button variant="secondary" onClick={aoFechar}>Fechar</Button>
        <Button onClick={() => aoAcompanhar(ocorrencia.idOcorrencia)}>Acompanhar relato</Button>
      </div>
    </div>}
  </Dialog>
}
