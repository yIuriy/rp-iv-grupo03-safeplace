package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;

@Entity
@Table(name = "incidentes")
public class IncidenteEntity extends OcorrenciaEntity {
    @Column(name = "situacao_risco")
    private String situacaoRisco;

    @Column(name = "potencial_dano")
    private String potencialDano;

    protected IncidenteEntity() {
    }

    public IncidenteEntity(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            String statusOcorrencia,
            AreaRisco area,
            ColaboradorEntidade colaborador,
            ColaboradorEntidade relator,
            String local,
            String descricao,
            PlanoDeAcaoEntity planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        super(idOcorrencia, dataOcorrencia, dataRegistro, statusOcorrencia, area, colaborador, relator, local, descricao, planoDeAcao);
        this.situacaoRisco = situacaoRisco;
        this.potencialDano = potencialDano;
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }
}
