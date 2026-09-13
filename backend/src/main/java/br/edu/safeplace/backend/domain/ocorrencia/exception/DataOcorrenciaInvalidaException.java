package br.edu.safeplace.backend.domain.ocorrencia.exception;

public class DataOcorrenciaInvalidaException extends RuntimeException {
    public DataOcorrenciaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
