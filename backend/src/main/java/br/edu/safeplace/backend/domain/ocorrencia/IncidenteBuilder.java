package br.edu.safeplace.backend.domain.ocorrencia;

/**
 * Builder da entidade Incidente (GoF, criacional, #240).
 * Separa a construção de Incidente da sua representação, compondo
 * dados comuns de ocorrência (via {@link OcorrenciaBuilder}) e atributos próprios
 * de incidente (situação de risco e potencial de dano) de forma fluente e nomeada.
 *
 * <p>O Builder não valida: a validação roda na camada validacao (OcorrenciaValidador).
 */
public class IncidenteBuilder extends OcorrenciaBuilder<IncidenteBuilder> {

    private String situacaoRisco;
    private String potencialDano;

    public IncidenteBuilder situacaoRisco(String situacaoRisco) {
        this.situacaoRisco = situacaoRisco;
        return this;
    }

    public IncidenteBuilder potencialDano(String potencialDano) {
        this.potencialDano = potencialDano;
        return this;
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }

    public Incidente build() {
        return new Incidente(this);
    }
}
