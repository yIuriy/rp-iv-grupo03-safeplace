package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Incidente de trabalho (quase-acidente). É a subclasse JOINED da tabela {@code incidentes} (#261).
 */
@Entity
@Table(name = "incidentes")
public class Incidente extends Ocorrencia {

    @Column(name = "situacao_risco")
    private String situacaoRisco;

    @Column(name = "potencial_dano")
    private String potencialDano;

    /** Exclusivo do JPA (decisão #254). */
    protected Incidente() {
    }

    /** Construtor a partir do Builder (GoF, criacional, #240). */
    public Incidente(IncidenteBuilder builder) {
        super(builder);
        this.situacaoRisco = builder.getSituacaoRisco() != null ? builder.getSituacaoRisco().trim() : null;
        this.potencialDano = builder.getPotencialDano() != null ? builder.getPotencialDano().trim() : null;
    }

    public static IncidenteBuilder builder() {
        return new IncidenteBuilder();
    }

    public static class Builder extends IncidenteBuilder {
    }

    @Override
    public TipoOcorrencia getTipoOcorrencia() {
        return TipoOcorrencia.INCIDENTE;
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }
}
