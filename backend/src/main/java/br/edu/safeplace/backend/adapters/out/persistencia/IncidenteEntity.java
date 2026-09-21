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

    public IncidenteEntity(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            String statusOcorrencia,
            AreaRiscoEntity area,
            ColaboradorEntidade colaborador,
            GestorDeSegurancaEntidade gestor,
            String local,
            String descricao,
            PlanoDeAcaoEntity planoDeAcao,
            String situacaoRisco,
            String potencialDano) {
        super(idOcorrencia, dataOcorrencia, dataRegistro, statusOcorrencia, area, colaborador, gestor, local, descricao, planoDeAcao);
        this.situacaoRisco = situacaoRisco;
        this.potencialDano = potencialDano;
    }

    public IncidenteEntity(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local, String descricao,
            PlanoDeAcaoEntity planoDeAcao, String situacaoRisco, String potencialDano) {
        this(idOcorrencia, dataOcorrencia, LocalDateTime.now(), "ABERTA", null, null, null, local, descricao,
                planoDeAcao, situacaoRisco, potencialDano);
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }
}