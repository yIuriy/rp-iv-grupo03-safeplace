package br.edu.safeplace.backend.domain.ocorrencia.exception;

/**
 * Inconsistência temporal no cronograma da ação (UC07, cenário de exceção I).
 */
public class PrazoAcaoInvalidoException extends RuntimeException {
    public PrazoAcaoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
