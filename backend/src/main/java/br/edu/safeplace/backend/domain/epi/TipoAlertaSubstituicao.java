package br.edu.safeplace.backend.domain.epi;

/**
 * Tipos de alerta preventivo para ciclo de vida e estoque de EPIs (RF21, UC10).
 */
public enum TipoAlertaSubstituicao {
    DESCARTE_PROXIMO("Substituição preventiva recomendada por aproximação da data estimada de descarte"),
    CA_PROXIMO_VENCIMENTO("Vencimento do Certificado de Aprovação (CA) iminente"),
    ESTOQUE_CRITICO("Saldo atual igual ou abaixo do estoque mínimo de segurança");

    private final String descricao;

    TipoAlertaSubstituicao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
