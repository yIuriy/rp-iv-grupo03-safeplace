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
        super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                colaborador, area, relator, local, planoDeAcao);

        // Situação de risco e potencial de dano: OcorrenciaValidador (issue #266).
        this.situacaoRisco = situacaoRisco != null ? situacaoRisco.trim() : null;
        this.potencialDano = potencialDano != null ? potencialDano.trim() : null;
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
