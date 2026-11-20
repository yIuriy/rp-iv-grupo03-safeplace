package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Registro persistido de uma prorrogação de prazo aceita (UC07, cenário alternativo I); entidade JPA
 * da tabela {@code prorrogacoes_prazo} (#262, decisão #254).
 */
@Entity
@Table(name = "prorrogacoes_prazo")
public class ProrrogacaoPrazo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_de_acao_id", nullable = false)
    private PlanoDeAcao planoDeAcao;

    @Column(name = "prazo_anterior", nullable = false)
    private LocalDate prazoAnterior;

    @Column(name = "novo_prazo", nullable = false)
    private LocalDate novoPrazo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exclusivo do JPA (decisão #254). */
    protected ProrrogacaoPrazo() {
    }

    public ProrrogacaoPrazo(PlanoDeAcao planoDeAcao, LocalDate prazoAnterior,
                            LocalDate novoPrazo, String justificativa) {
        this.planoDeAcao = planoDeAcao;
        this.prazoAnterior = prazoAnterior;
        this.novoPrazo = novoPrazo;
        this.justificativa = justificativa;
        this.criadoEm = LocalDateTime.now();
    }

    @PrePersist
    void preencherCriadoEm() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public PlanoDeAcao getPlanoDeAcao() {
        return planoDeAcao;
    }

    public Integer getIdAcao() {
        return planoDeAcao != null ? planoDeAcao.getId() : null;
    }

    public LocalDate getPrazoAnterior() {
        return prazoAnterior;
    }

    public LocalDate getNovoPrazo() {
        return novoPrazo;
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
        if (!(outro instanceof ProrrogacaoPrazo prorrogacao)) {
            return false;
        }
        return getId() != null && getId().equals(prorrogacao.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
