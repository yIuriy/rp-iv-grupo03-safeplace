/*
 * Exceção de domínio para os impedimentos de entrega do UC12: sem saldo (exceção II), CA vencido
 * (regra 1), treinamento obrigatório vencido (exceção I), EPI em situação não permitida ou
 * colaborador não cadastrado/inativo. A mensagem informa o motivo ao operador (US11, critério 5).
 */
package br.edu.safeplace.backend.domain.emprestimo.exception;

public class EntregaImpedidaException extends RuntimeException {
    public EntregaImpedidaException(String motivo) {
        super(motivo);
    }
}
