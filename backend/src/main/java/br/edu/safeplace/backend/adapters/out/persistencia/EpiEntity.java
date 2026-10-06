package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

/**
 * Saldo de um lote de EPI (decisão #124). CA e validade do CA vêm apenas do modelo do lote (V28).
 */
@Entity
@Table(name = "epis")
public class EpiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "vida_util_dias")
    private Integer vidaUtilDias;

    @Column(name = "especificacao_descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "especificacao_classificacao", length = 80)
    private String classificacao;

    @Column(columnDefinition = "text")
    private String localizacao;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteEpiEntity lote;

    public EpiEntity() {
    }

    public EpiEntity(Integer id, String nome, Integer quantidade, Integer estoqueMinimo, String status,
                     Integer vidaUtilDias, String descricao, String classificacao, String localizacao,
                     LoteEpiEntity lote) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
        this.status = status;
        this.vidaUtilDias = vidaUtilDias;
        this.descricao = descricao;
        this.classificacao = classificacao;
        this.localizacao = localizacao;
        this.lote = lote;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public String getStatus() {
        return status;
    }

    public Integer getVidaUtilDias() {
        return vidaUtilDias;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public LoteEpiEntity getLote() {
        return lote;
    }
}
