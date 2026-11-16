// Etapas finais do diálogo de entrega: confirmação de duplicidade com justificativa obrigatória
// (UC12, exceção III; US11, critério 6) e conclusão com o termo de cautela registrado (UC12, passo 6;
// US11, critério 1).

import { useState, type FormEvent } from 'react'
import { Alert, Button, TextArea } from '../../shared/components'
import { focarCampo } from '../../shared/formularios/envioFormulario'
import { formatarData } from '../../shared/formatos'
import type { Emprestimo } from './api'
import { validarJustificativa } from './validacaoEntrega'

/** O servidor recusou a entrega por duplicidade; a nova tentativa leva confirmação e justificativa. */
export function DuplicidadeConfirmacao({ mensagem, falha, enviando, aoVoltar, aoConfirmar }: {
  mensagem: string
  falha: string
  enviando: boolean
  aoVoltar: () => void
  aoConfirmar: (justificativa: string) => void
}) {
  const [erro, setErro] = useState<string>()

  function submeter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const form = event.currentTarget
    const justificativa = String(new FormData(form).get('justificativaSubstituicao') ?? '').trim()
    const pendencia = validarJustificativa(justificativa)
    setErro(pendencia)
    if (!pendencia) {
      aoConfirmar(justificativa)
      return
    }
    focarCampo(form, 'justificativaSubstituicao')
  }

  return <form className="sp-form" onSubmit={submeter} noValidate>
    <Alert tone="warning" title="Item do mesmo tipo em posse" live="assertive">{mensagem}</Alert>
    <TextArea label="Justificativa da substituição" name="justificativaSubstituicao" rows={3} required autoFocus
      error={erro} helper="Motivo da nova entrega sem a devolução do item anterior." />
    {falha && <Alert tone="danger" title="Entrega não registrada" live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="quiet" onClick={aoVoltar} disabled={enviando}>Voltar</Button>
      <Button type="submit" icon="check" loading={enviando}>Confirmar substituição</Button>
    </div>
  </form>
}

/** Confirmação do registro: posse, prazo, saída do estoque e o texto do termo de cautela. */
export function EntregaConcluida({ emprestimo, aoFechar }: { emprestimo: Emprestimo; aoFechar: () => void }) {
  return <div className="sp-dialog-body">
    <Alert tone="success" title="Entrega registrada" live="polite">
      {emprestimo.epiNome} está em posse de {emprestimo.colaboradorNome}, com devolução prevista para{' '}
      {formatarData(emprestimo.dataDevolucaoPrevista)}. Uma unidade saiu do estoque.
    </Alert>
    <div className="sp-secao">
      <h3 className="sp-type-heading-sm">Termo de cautela</h3>
      <p className="sp-termo">{emprestimo.termoCautela}</p>
    </div>
    <div className="sp-dialog-actions"><Button icon="check" autoFocus onClick={aoFechar}>Fechar</Button></div>
  </div>
}
