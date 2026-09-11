package br.edu.safeplace.backend.domain.usuario.exception;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Credenciais inválidas ou usuário sem permissão de acesso.");
    }

    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
