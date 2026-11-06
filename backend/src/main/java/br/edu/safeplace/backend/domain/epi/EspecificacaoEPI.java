package br.edu.safeplace.backend.domain.epi;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Dados descritivos do EPI, embutidos nas colunas da tabela epis (#258). */
@Embeddable
public class EspecificacaoEPI {
    @Column(name = "especificacao_descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "estoque_minimo", nullable = false)
    private int quantidadeMinima;

    @Enumerated(EnumType.STRING)
    @Column(name = "especificacao_classificacao", length = 80)
    private ClassificacaoEPI classificacao;

    /** Exclusivo do JPA (decisão #254). */
    protected EspecificacaoEPI() {
    }

    public EspecificacaoEPI(
            String descricao,
            int quantidadeMinima,
            ClassificacaoEPI classificacao) {

        this.descricao = descricao;
        this.quantidadeMinima = quantidadeMinima;
        this.classificacao = classificacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public ClassificacaoEPI getClassificacao() {
        return classificacao;
    }
}
