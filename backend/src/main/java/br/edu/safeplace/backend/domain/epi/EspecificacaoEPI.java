package br.edu.safeplace.backend.domain.epi;

public class EspecificacaoEPI {
    private final String descricao;
    private final int quantidadeMinima;
    private final ClassificacaoEPI classificacao;

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
