package br.edu.safeplace.backend.domain.area_risco;

import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/**
 * Representa um setor físico da planta industrial (RF05 / UC03).
 * Mapeado conforme o Diagrama de Classes UML.
 */
public class Setor {

    private final Integer id;
    private final String nome;
    private final String descricao;
    private final String localizacao;
    private final NivelPerigo status;

    public Setor(Integer id, String nome, String descricao, String localizacao, NivelPerigo status) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do setor é obrigatório.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status/Nível de perigo do setor é obrigatório.");
        }

        this.id = id;
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.localizacao = localizacao != null ? localizacao.trim() : null;
        this.status = status;
    }

    public static Setor novo(String nome, String descricao, String localizacao, NivelPerigo status) {
        return new Setor(null, nome, descricao, localizacao, status);
    }

    public static Setor reconstituir(Integer id, String nome, String descricao, String localizacao, NivelPerigo status) {
        return new Setor(id, nome, descricao, localizacao, status);
    }

    public Setor atualizar(String novoNome, String novaDescricao, String novaLocalizacao, NivelPerigo novoStatus) {
        return new Setor(this.id, novoNome, novaDescricao, novaLocalizacao, novoStatus);
    }

    /**
     * Operação {@code obterDadosPlanta()} do diagrama de classes.
     */
    public Setor obterDadosPlanta() {
        return this;
    }

    /**
     * Operação {@code validarLimitesSetor(localOrigem: int)} do diagrama de classes.
     */
    public boolean validarLimitesSetor(int localOrigem) {
        return localOrigem >= 0;
    }

    /**
     * Operação {@code buscarSetoresFisicos()} do diagrama de classes.
     */
    public List<Setor> buscarSetoresFisicos() {
        return List.of(this);
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

    public String getLocalizacao() {
        return localizacao;
    }

    public NivelPerigo getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Setor setor)) {
            return false;
        }
        if (id != null && setor.id != null) {
            return id.equals(setor.id);
        }
        return nome.equalsIgnoreCase(setor.nome);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : Objects.hashCode(nome.toLowerCase());
    }
}
