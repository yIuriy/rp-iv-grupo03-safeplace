package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "epis")
public class EpiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(name = "numero_ca", nullable = false, length = 80)
    private String numeroCa;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "data_validade_ca")
    private LocalDate dataValidadeCa;

    @Column(name = "vida_util_dias")
    private Integer vidaUtilDias;

    @Column(name = "especificacao_descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "especificacao_classificacao", length = 80)
    private String classificacao;

    public EpiEntity() {
    }

    public EpiEntity(Integer id, String nome, String numeroCa, Integer quantidade,
            Integer estoqueMinimo, String status, LocalDate dataValidadeCa, Integer vidaUtilDias, String descricao,
            String classificacao) {
        this.id = id;
        this.nome = nome;
        this.numeroCa = numeroCa;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
        this.status = status;
        this.dataValidadeCa = dataValidadeCa;
        this.vidaUtilDias = vidaUtilDias;
        this.descricao = descricao;
        this.classificacao = classificacao;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroCa() {
        return numeroCa;
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

    public LocalDate getDataValidadeCa() {
        return dataValidadeCa;
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
}
