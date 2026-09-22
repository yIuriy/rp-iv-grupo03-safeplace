/*
 * Exceção para a exceção III do UC12: o colaborador já possui um EPI do mesmo tipo em posse
 * ativa. Não é bloqueio absoluto: a entrega prossegue quando o operador confirma a substituição e
 * informa a justificativa (US11, regra 4 e critério 6).
 */
package br.edu.safeplace.backend.domain.emprestimo.exception;

public class DuplicidadeDePosseException extends RuntimeException {
    private final Integer emprestimoAtivoId;

    public DuplicidadeDePosseException(Integer emprestimoAtivoId, String descricaoDoTipo) {
        super("O colaborador já possui um EPI do tipo " + descricaoDoTipo
                + " em posse ativa (empréstimo " + emprestimoAtivoId
                + "). Confirme a substituição e informe a justificativa.");
        this.emprestimoAtivoId = emprestimoAtivoId;
    }

    public Integer getEmprestimoAtivoId() {
        return emprestimoAtivoId;
    }
}
