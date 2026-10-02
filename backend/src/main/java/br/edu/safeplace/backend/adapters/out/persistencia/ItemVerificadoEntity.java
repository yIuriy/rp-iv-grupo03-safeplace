/*
 * Entidade JPA da tabela itens_verificados (V24, issue #134): referência avaliada, resultado e
 * observações de um item de uma única inspeção.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

@Entity
@Table(name = "itens_verificados")
public class ItemVerificadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspecao_id", nullable = false)
    private InspecaoEntity inspecao;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    private boolean conformidade;

    @Column(columnDefinition = "text")
    private String observacoes;

    protected ItemVerificadoEntity() {
    }

    public ItemVerificadoEntity(InspecaoEntity inspecao, String descricao, boolean conformidade, String observacoes) {
        this.inspecao = inspecao;
        this.descricao = descricao;
        this.conformidade = conformidade;
        this.observacoes = observacoes;
    }

    public Integer getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isConformidade() {
        return conformidade;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
