/*
 * Exceção lançada quando o empréstimo consultado ou devolvido não existe.
 */
package br.edu.safeplace.backend.domain.emprestimo.exception;

public class EmprestimoNaoEncontradoException extends RuntimeException {
    public EmprestimoNaoEncontradoException(Integer id) {
        super("Empréstimo não encontrado: " + id);
    }
}
