package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidentes")
public class IncidenteEntity extends OcorrenciaEntity {
    @Column(name = "situacao_risco")
    private String situacaoRisco;

    @Column(name = "potencial_dano")
    private String potencialDano;

    protected IncidenteEntity() {
    }

    public IncidenteEntity(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local, String descricao,
            PlanoDeAcaoEntity planoDeAcao, String situacaoRisco, String potencialDano) {
        super(idOcorrencia, dataOcorrencia, local, descricao, planoDeAcao);
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