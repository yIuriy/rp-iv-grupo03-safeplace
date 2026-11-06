package br.edu.safeplace.backend.domain.epi.exception;

import br.edu.safeplace.backend.domain.epi.StatusEpi;

public class TransicaoStatusEpiInvalidaException extends RuntimeException {
    public TransicaoStatusEpiInvalidaException(Integer epiId, StatusEpi statusAtual, StatusEpi novoStatus) {
        super(String.format("O EPI com ID %s não pode passar de %s para %s.",
                epiId != null ? epiId : "N/A", statusAtual, novoStatus));
    }
}
