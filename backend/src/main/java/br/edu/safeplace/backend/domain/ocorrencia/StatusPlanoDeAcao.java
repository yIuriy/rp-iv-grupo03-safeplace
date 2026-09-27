package br.edu.safeplace.backend.domain.ocorrencia;

import java.util.Optional;

/**
 * Vocabulário do atributo {@code status : String} de {@code PlanoDeAcao} (UC07, issue #97).
 *
 * <p>O diagrama de classes tipa {@code status} como texto; esta enumeração fixa os valores aceitos
 * pelo contrato sem trocar o atributo. Apenas a execução da ação é registrada: {@code ABERTA} no
 * cadastro e {@code CONCLUIDA} quando o Gestor confirma a execução. Prazo a vencer e atraso não são
 * estados gravados; derivam do prazo e da data consultada (decisão 2.3 da correspondência do plano de
 * ação).</p>
 */
public enum StatusPlanoDeAcao {
    ABERTA {
        @Override
        public boolean podeTransicaoPara(StatusPlanoDeAcao novoStatus) {
            return novoStatus == CONCLUIDA;
        }
    },
    CONCLUIDA {
        @Override
        public boolean podeTransicaoPara(StatusPlanoDeAcao novoStatus) {
            return false;
        }
    };

    public abstract boolean podeTransicaoPara(StatusPlanoDeAcao novoStatus);

    /**
     * Reconhece um valor gravado no atributo textual. Valores anteriores ao contrato (por exemplo,
     * textos livres aceitos pela API antiga) não são convertidos por suposição: o resultado é vazio
     * e a normalização fica a cargo da migração de #98.
     */
    public static Optional<StatusPlanoDeAcao> reconhecer(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        for (StatusPlanoDeAcao status : values()) {
            if (status.name().equalsIgnoreCase(valor.trim())) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
