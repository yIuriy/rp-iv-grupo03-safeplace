package br.edu.safeplace.backend.domain.ocorrencia.exception;

public class OcorrenciaNaoEncontradaException extends RuntimeException {
    public OcorrenciaNaoEncontradaException(Integer id) {
        super(String.format("Ocorrência não encontrada com o identificador %d.", id));
    }
}
