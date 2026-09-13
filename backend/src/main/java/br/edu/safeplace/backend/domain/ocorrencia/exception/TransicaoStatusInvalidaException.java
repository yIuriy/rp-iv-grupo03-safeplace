package br.edu.safeplace.backend.domain.ocorrencia.exception;

import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;

public class TransicaoStatusInvalidaException extends RuntimeException {
    public TransicaoStatusInvalidaException(StatusOcorrencia statusAtual, StatusOcorrencia novoStatus) {
        super(String.format("Transição de status inválida de %s para %s.", statusAtual, novoStatus));
    }
}
