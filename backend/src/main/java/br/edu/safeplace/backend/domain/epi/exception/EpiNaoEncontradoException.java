package br.edu.safeplace.backend.domain.epi.exception;

public class EpiNaoEncontradoException extends RuntimeException {
    public EpiNaoEncontradoException(Integer id) {
        super("EPI com ID " + id + " não encontrado.");
    }

    public EpiNaoEncontradoException(String message) {
        super(message);
    }
}
