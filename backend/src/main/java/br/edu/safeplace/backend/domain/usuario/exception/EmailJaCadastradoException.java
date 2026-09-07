package br.edu.safeplace.backend.domain.usuario.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String message) {
        super(message);
    }
}
