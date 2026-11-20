package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidade de domínio ProjecaoSubstituicao (RF21, UC10, US21, issue #108) e entidade JPA da tabela
 * projecoes_substituicao (#259). Segue o Diagrama de Classes: dataProjecao, dataEstimadaDescarte e
 * justificativa, associada ao EPI analisado; {@link #getEpiId()} serve aos DTOs que continuam expondo ids.
 */
@Entity
@Table(name = "projecoes_substituicao")
public class ProjecaoSubstituicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "epi_id", nullable = false)
    private Epi epi;

    @Column(name = "data_projecao", nullable = false)
    private LocalDate dataProjecao;

    @Column(name = "data_estimada_descarte", nullable = false)
    private LocalDate dataEstimadaDescarte;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exclusivo do JPA (decisão #254). */
    protected ProjecaoSubstituicao() {
    }

    public ProjecaoSubstituicao(Epi epi, LocalDate dataProjecao,
                                LocalDate dataEstimadaDescarte, String justificativa) {
        this.epi = epi;
        this.dataProjecao = dataProjecao;
        this.dataEstimadaDescarte = dataEstimadaDescarte;
        this.justificativa = justificativa != null ? justificativa.trim() : null;
        this.criadoEm = LocalDateTime.now();
    }

    @PrePersist
    void preencherCriadoEm() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    /**
     * Verifica se o descarte estimado está dentro da janela de alerta preventivo (<= 30 dias).
     */
    public boolean isProximoDoDescarte(LocalDate dataReferencia, long diasAntecedencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        long diasRestantes = ChronoUnit.DAYS.between(dataReferencia, this.dataEstimadaDescarte);
        return diasRestantes >= 0 && diasRestantes <= diasAntecedencia;
    }

    public boolean isVencido(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        return this.dataEstimadaDescarte.isBefore(dataReferencia);
    }

    public long calcularDiasRestantes(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        return ChronoUnit.DAYS.between(dataReferencia, this.dataEstimadaDescarte);
    }

    public Integer getId() {
        return id;
    }

    public Epi getEpi() {
        return epi;
    }

    public Integer getEpiId() {
        return epi != null ? epi.getId() : null;
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

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ProjecaoSubstituicao projecao)) {
            return false;
        }
        return getId() != null && getId().equals(projecao.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ProjecaoSubstituicao{" +
                "id=" + id +
                ", dataProjecao=" + dataProjecao +
                ", dataEstimadaDescarte=" + dataEstimadaDescarte +
                ", justificativa='" + justificativa + '\'' +
                '}';
    }
}
