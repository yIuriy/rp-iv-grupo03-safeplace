// Diálogo "Registrar devolução" (UC12, alternativo I; US11, regra 5 e critério 4): mostra o titular e
// o item em posse, pede a condição do item (apto, manutenção ou descarte), confirma o destino antes do
// envio e exibe a recusa do servidor, como um empréstimo já encerrado por outra pessoa. O fluxo de envio
// vem do hook useEnvioFormulario (Template Method, #243).

import { useId, useState } from 'react'
import { Alert, Button, Dialog, Radio } from '../../shared/components'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import { formatarData, formatarDataHora } from '../../shared/formatos'
import {
  descreverFalhaDeDevolucao, estadosDeDevolucao, registrarDevolucao, type Emprestimo, type EstadoDevolucao,
} from './api'
import { destinoDoEstado, opcaoDoEstado } from './formatos'

export function DevolucaoDialog({ emprestimo, aoFechar, aoDevolver }: {
  emprestimo: Emprestimo | null
  aoFechar: () => void
  aoDevolver: (devolvido: Emprestimo) => void
}) {
  return <Dialog open={emprestimo !== null} title="Registrar devolução" onClose={aoFechar}
    description="Confira o item recebido e informe sua condição antes de concluir.">
    {emprestimo && <DevolucaoFormulario key={emprestimo.id} emprestimo={emprestimo} aoFechar={aoFechar} aoDevolver={aoDevolver} />}
  </Dialog>
}

function DevolucaoFormulario({ emprestimo, aoFechar, aoDevolver }: {
  emprestimo: Emprestimo
  aoFechar: () => void
  aoDevolver: (devolvido: Emprestimo) => void
}) {
  const idErro = useId()
  const [estado, setEstado] = useState<EstadoDevolucao | null>(null)

  const { enviando, erros, falha, onSubmit } = useEnvioFormulario({
    ler: () => estado,
    validar: escolhido => escolhido ? { dados: escolhido, erros: {} } : { erros: { estado: 'Informe a condição do item devolvido.' } },
    ordem: ['estado'],
    enviar: async escolhido => {
      aoDevolver(await registrarDevolucao(emprestimo.id, escolhido))
      aoFechar()
    },
    descreverFalha: descreverFalhaDeDevolucao,
  }, {} as { estado?: string })
  // A pendência some ao escolher uma condição, como antes.
  const erro = estado ? '' : erros.estado ?? ''

  return <form className="sp-form" onSubmit={onSubmit} noValidate>
    <dl className="sp-detalhes">
      <div><dt>Colaborador</dt><dd>{emprestimo.colaboradorNome}</dd></div>
      <div><dt>Equipamento devolvido</dt><dd>{emprestimo.epiNome} (código {emprestimo.epiId})</dd></div>
      <div><dt>Entregue em</dt><dd>{formatarDataHora(emprestimo.dataRetirada)}</dd></div>
      <div><dt>Devolução prevista</dt>
        <dd>{formatarData(emprestimo.dataDevolucaoPrevista)}{emprestimo.atrasado && ' (prazo vencido)'}</dd></div>
    </dl>
    <fieldset className="sp-fieldset" aria-describedby={erro ? idErro : undefined}>
      <legend className="sp-field-label">Condição do item</legend>
      <div className="sp-opcoes sp-opcoes--coluna">
        {estadosDeDevolucao.map(opcao => <Radio key={opcao} label={opcaoDoEstado[opcao]} name="estado" value={opcao}
          checked={estado === opcao} aria-invalid={erro ? true : undefined}
          onChange={() => setEstado(opcao)} />)}
      </div>
      {erro && <p id={idErro} className="sp-field-message sp-field-message--error">{erro}</p>}
    </fieldset>
    {estado && <p className="sp-destino" role="status">{destinoDoEstado[estado]}</p>}
    {falha && <Alert tone="danger" title="Devolução não registrada" live="assertive">{falha}</Alert>}
    <p className="sp-dialog-note">A data, a hora e o responsável pela devolução são registrados na confirmação.</p>
    <div className="sp-dialog-actions">
      <Button variant="quiet" onClick={aoFechar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" icon="check" loading={enviando}>Confirmar devolução</Button>
    </div>
  </form>
}
