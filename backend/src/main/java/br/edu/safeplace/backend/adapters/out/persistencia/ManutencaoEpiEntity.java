package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "manutencoes_epi")
public class ManutencaoEpiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "epi_id", nullable = false)
    private Integer epiId;

    @Column(name = "data_manutencao", nullable = false)
    private LocalDateTime dataManutencao;

    @Column(name = "tipo_manutencao", length = 30)
    private String tipoManutencao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(name = "responsavel_manutencao", nullable = false, length = 160)
    private String responsavelManutencao;

    @Column(name = "responsavel_id", nullable = false)
    private Integer responsavelId;

    protected ManutencaoEpiEntity() {
    }

    public ManutencaoEpiEntity(Integer id, Integer epiId, LocalDateTime dataManutencao,
                               String tipoManutencao, String descricao, String resultado,
                               String responsavelManutencao, Integer responsavelId) {
        this.id = id;
        this.epiId = epiId;
        this.dataManutencao = dataManutencao;
        this.tipoManutencao = tipoManutencao;
        this.descricao = descricao;
        this.resultado = resultado;
        this.responsavelManutencao = responsavelManutencao;
        this.responsavelId = responsavelId;
    }

    public Integer getId() { return id; }
    public Integer getEpiId() { return epiId; }
    public LocalDateTime getDataManutencao() { return dataManutencao; }
    public String getTipoManutencao() { return tipoManutencao; }
    public String getDescricao() { return descricao; }
    public String getResultado() { return resultado; }
    public String getResponsavelManutencao() { return responsavelManutencao; }
    public Integer getResponsavelId() { return responsavelId; }
}
