package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "projecoes_substituicao")
public class ProjecaoSubstituicaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "epi_id", nullable = false)
    private Integer epiId;

    @Column(name = "data_projecao", nullable = false)
    private LocalDate dataProjecao;

    @Column(name = "data_estimada_descarte", nullable = false)
    private LocalDate dataEstimadaDescarte;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected ProjecaoSubstituicaoEntity() {
    }

    public ProjecaoSubstituicaoEntity(Integer id, Integer epiId, LocalDate dataProjecao,
                                      LocalDate dataEstimadaDescarte, String justificativa) {
        this.id = id;
        this.epiId = epiId;
        this.dataProjecao = dataProjecao;
        this.dataEstimadaDescarte = dataEstimadaDescarte;
        this.justificativa = justificativa;
        this.criadoEm = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public LocalDate getDataProjecao() {
        return dataProjecao;
    }

    public LocalDate getDataEstimadaDescarte() {
        return dataEstimadaDescarte;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
