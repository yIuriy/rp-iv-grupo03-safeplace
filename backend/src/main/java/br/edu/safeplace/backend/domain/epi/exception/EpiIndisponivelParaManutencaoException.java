package br.edu.safeplace.backend.domain.epi.exception;

import br.edu.safeplace.backend.domain.epi.StatusEpi;

public class EpiIndisponivelParaManutencaoException extends RuntimeException {
    private final Integer epiId;
    private final StatusEpi statusAtual;

    public EpiIndisponivelParaManutencaoException(Integer epiId, StatusEpi statusAtual, String motivo) {
        super(String.format(
                "EPI com ID %s não pode transitar para manutenção no estado atual '%s'. Motivo: %s",
                epiId != null ? epiId : "N/A", statusAtual, motivo
        ));
        this.epiId = epiId;
        this.statusAtual = statusAtual;
    }

    public EpiIndisponivelParaManutencaoException(String message) {
        super(message);
        this.epiId = null;
        this.statusAtual = null;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public StatusEpi getStatusAtual() {
        return statusAtual;
    }
}
