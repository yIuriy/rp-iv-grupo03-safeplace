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
@Table(name = "prorrogacoes_prazo")
public class ProrrogacaoPrazoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plano_de_acao_id", nullable = false)
    private Integer planoDeAcaoId;

    @Column(name = "prazo_anterior", nullable = false)
    private LocalDate prazoAnterior;

    @Column(name = "novo_prazo", nullable = false)
    private LocalDate novoPrazo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected ProrrogacaoPrazoEntity() {
    }

    public ProrrogacaoPrazoEntity(
            Integer id,
            Integer planoDeAcaoId,
            LocalDate prazoAnterior,
            LocalDate novoPrazo,
            String justificativa,
            LocalDateTime criadoEm) {
        this.id = id;
        this.planoDeAcaoId = planoDeAcaoId;
        this.prazoAnterior = prazoAnterior;
        this.novoPrazo = novoPrazo;
        this.justificativa = justificativa;
        this.criadoEm = criadoEm != null ? criadoEm : LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public Integer getPlanoDeAcaoId() {
        return planoDeAcaoId;
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
}
