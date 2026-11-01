package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidade de domínio ProjecaoSubstituicao (RF21, UC10, US21, issue #108).
 * Segue o Diagrama de Classes: dataProjecao, dataEstimadaDescarte e justificativa,
 * associada ao EPI analisado.
 */
public class ProjecaoSubstituicao {
    private final Integer id;
    private final Integer epiId;
    private final LocalDate dataProjecao;
    private final LocalDate dataEstimadaDescarte;
    private final String justificativa;

    public ProjecaoSubstituicao(Integer id, Integer epiId, LocalDate dataProjecao,
                               LocalDate dataEstimadaDescarte, String justificativa) {
        this.id = id;
        this.epiId = epiId;
        this.dataProjecao = dataProjecao;
        this.dataEstimadaDescarte = dataEstimadaDescarte;
        this.justificativa = justificativa != null ? justificativa.trim() : null;
    }

    public static ProjecaoSubstituicao nova(Integer epiId, LocalDate dataProjecao,
                                           LocalDate dataEstimadaDescarte, String justificativa) {
        return new ProjecaoSubstituicao(null, epiId, dataProjecao, dataEstimadaDescarte, justificativa);
    }

    public static ProjecaoSubstituicao reconstituir(Integer id, Integer epiId, LocalDate dataProjecao,
                                                   LocalDate dataEstimadaDescarte, String justificativa) {
        return new ProjecaoSubstituicao(id, epiId, dataProjecao, dataEstimadaDescarte, justificativa);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjecaoSubstituicao that = (ProjecaoSubstituicao) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(epiId, that.epiId) &&
                Objects.equals(dataProjecao, that.dataProjecao) &&
                Objects.equals(dataEstimadaDescarte, that.dataEstimadaDescarte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, epiId, dataProjecao, dataEstimadaDescarte);
    }

    @Override
    public String toString() {
        return "ProjecaoSubstituicao{" +
                "id=" + id +
                ", epiId=" + epiId +
                ", dataProjecao=" + dataProjecao +
                ", dataEstimadaDescarte=" + dataEstimadaDescarte +
                ", justificativa='" + justificativa + '\'' +
                '}';
    }
}
