package br.edu.safeplace.backend.domain.capacitacao;

/**
 * Situação do treinamento (US10; RF10). Os valores de vencimento só existem para treinamento com
 * validade; sem validade o treinamento permanece {@code REALIZADO}.
 */
public enum StatusTreinamento {
    PENDENTE,
    REALIZADO,
    PROXIMO_DO_VENCIMENTO,
    VENCIDO;

    /** Indica se o treinamento ainda comprova a exigência de capacitação. */
    public boolean atendeExigencia() {
        return this == REALIZADO || this == PROXIMO_DO_VENCIMENTO;
    }
}
