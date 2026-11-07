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
public non-sealed class Incidente extends Ocorrencia {

    @Column(name = "situacao_risco")
    private String situacaoRisco;

    @Column(name = "potencial_dano")
    private String potencialDano;

    /** Exclusivo do JPA (decisão #254). */
    protected Incidente() {
    }

    /** Construtor a partir do Builder (GoF, criacional, #240). */
    public Incidente(IncidenteBuilder builder) {
        super(builder.getIdOcorrencia(), builder.getDescricao(), builder.getDataOcorrencia(),
                builder.getDataRegistro() != null ? builder.getDataRegistro() : LocalDateTime.now(),
                builder.getStatusOcorrencia() != null ? builder.getStatusOcorrencia() : StatusOcorrencia.ABERTA,
                builder.getColaborador(), builder.getArea(), builder.getRelator(),
                builder.getLocalInformado(), builder.getPlanoDeAcao());
        this.situacaoRisco = builder.getSituacaoRisco() != null ? builder.getSituacaoRisco().trim() : null;
        this.potencialDano = builder.getPotencialDano() != null ? builder.getPotencialDano().trim() : null;
    }

    public static IncidenteBuilder builder() {
        return new IncidenteBuilder();
    }

    public static class Builder extends IncidenteBuilder {
    }

    @Deprecated
    public Incidente(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            String local,
            PlanoDeAcao planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        this(new IncidenteBuilder()
                .idOcorrencia(idOcorrencia)
                .descricao(descricao)
                .dataOcorrencia(dataOcorrencia)
                .dataRegistro(dataRegistro)
                .statusOcorrencia(statusOcorrencia)
                .colaborador(colaborador)
                .area(area)
                .relator(relator)
                .localInformado(local)
                .planoDeAcao(planoDeAcao)
                .situacaoRisco(situacaoRisco)
                .potencialDano(potencialDano));
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
