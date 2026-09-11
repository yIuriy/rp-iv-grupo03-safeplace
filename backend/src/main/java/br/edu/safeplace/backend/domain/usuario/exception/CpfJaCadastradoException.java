package br.edu.safeplace.backend.domain.usuario.exception;

public class CpfJaCadastradoException extends RuntimeException {
    public CpfJaCadastradoException(String message) {
        super(message);
    }
}
