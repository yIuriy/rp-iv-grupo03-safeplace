package br.edu.safeplace.backend.domain.capacitacao;

import java.util.Collection;
import java.util.Comparator;

/**
 * Situação da certificação (UC02, cenário alternativo IV). A ordem das constantes vai da menos
 * para a mais crítica.
 */
public enum StatusCertificacao {
    EM_DIA,
    PROXIMA_DO_VENCIMENTO,
    VENCIDA;

    /**
     * Situação consolidada de um conjunto de certificações: a mais crítica entre elas, ou
     * {@code null} quando não há certificação (ausência é pendência, não situação).
     */
    public static StatusCertificacao maisCritica(Collection<StatusCertificacao> situacoes) {
        if (situacoes == null) {
            return null;
        }
        return situacoes.stream()
                .filter(s -> s != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
