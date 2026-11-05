/*
 * Item verificado em uma inspeção (diagrama de classes: descricao, conformidade e observacoes). Na
 * US09, é a norma ou referência avaliada com seu resultado (issue #134, seção 2.2 da correspondência).
 */
package br.edu.safeplace.backend.domain.inspecao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * {@code conformidade} verdadeira indica conformidade; falsa, não conformidade (US09, critério 4).
 * {@code observacoes} é opcional, porque a US09 deixa essa restrição a confirmar. Descrição e
 * resultado são validados por {@code ItensValidosElo}, na camada de validação (issue #263).
 */
@Entity
@Table(name = "itens_verificados")
public class ItemVerificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspecao_id", nullable = false)
    private Inspecao inspecao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private Boolean conformidade;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    protected ItemVerificado() {
    }

    public ItemVerificado(Integer id, String descricao, Boolean conformidade, String observacoes) {
        this.id = id;
        this.descricao = descricao != null ? descricao.trim() : null;
        this.conformidade = conformidade;
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }

    public static ItemVerificado novo(String descricao, Boolean conformidade, String observacoes) {
        return new ItemVerificado(null, descricao, conformidade, observacoes);
    }

    void vincularInspecao(Inspecao inspecao) {
        this.inspecao = inspecao;
    }

    public Integer getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isConformidade() {
        return Boolean.TRUE.equals(conformidade);
    }

    /** Resultado como informado; nulo enquanto não validado. */
    public Boolean getConformidade() {
        return conformidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ItemVerificado item)) {
            return false;
        }
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
