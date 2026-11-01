/*
 * Item verificado em uma inspeção (diagrama de classes: descricao, conformidade e observacoes). Na
 * US09, é a norma ou referência avaliada com seu resultado (issue #134, seção 2.2 da correspondência).
 */
package br.edu.safeplace.backend.domain.inspecao;

/**
 * {@code conformidade} verdadeira indica conformidade; falsa, não conformidade (US09, critério 4).
 * {@code observacoes} é opcional, porque a US09 deixa essa restrição a confirmar. Descrição e
 * resultado são validados por {@code ItensValidosElo}, na camada de validação (issue #263).
 */
public class ItemVerificado {

    private final Integer id;
    private final String descricao;
    private final Boolean conformidade;
    private final String observacoes;

    public ItemVerificado(Integer id, String descricao, Boolean conformidade, String observacoes) {
        this.id = id;
        this.descricao = descricao != null ? descricao.trim() : null;
        this.conformidade = conformidade;
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }

    public static ItemVerificado novo(String descricao, Boolean conformidade, String observacoes) {
        return new ItemVerificado(null, descricao, conformidade, observacoes);
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
}
