/*
 * Exceção lançada ao tentar devolver um empréstimo que já foi encerrado.
 */
package br.edu.safeplace.backend.domain.emprestimo.exception;

public class EmprestimoJaEncerradoException extends RuntimeException {
    public EmprestimoJaEncerradoException(Integer id) {
        super("Empréstimo " + id + " já foi devolvido e não aceita nova devolução.");
    }
}
