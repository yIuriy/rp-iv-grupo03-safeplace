package br.edu.safeplace.backend.domain.epi.exception;

import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;

public class EspecificacaoEPI {
    private final String descricao;
    private final int quantidadeMinima;
    private final ClassificacaoEPI classificacao;

    public EspecificacaoEPI(
            String descricao,
            int quantidadeMinima,
            ClassificacaoEPI classificacao) {
        if (quantidadeMinima < 0) {
            throw new IllegalArgumentException(
                    "Quantidade mínima não pode ser negativa.");
        }

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
