package br.edu.safeplace.backend.domain.area_risco;

public class AreaRisco {
    private final Integer id;
    private final String nome;
    private final String descricao;
    private final String nivelRisco;

    public AreaRisco(Integer id, String nome, String descricao, String nivelRisco) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da área de risco é obrigatório.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.nivelRisco = nivelRisco != null ? nivelRisco.trim() : null;
    }

    public static AreaRisco novo(String nome, String descricao, String nivelRisco) {
        return new AreaRisco(null, nome, descricao, nivelRisco);
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }
}
