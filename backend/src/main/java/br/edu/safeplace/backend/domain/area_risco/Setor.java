package br.edu.safeplace.backend.domain.area_risco;

import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa um setor físico da planta industrial (RF05 / UC03).
 * Mapeado conforme o Diagrama de Classes UML.
 */
@Entity
@Table(name = "setores")
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelPerigo status;

    protected Setor() {
    }

    /** Nome e status são validados por SetorValidador antes de salvar (issue #263). */
    public Setor(Integer id, String nome, String descricao, String localizacao, NivelPerigo status) {
        this.id = id;
        this.nome = nome != null ? nome.trim() : null;
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
        if (getId() != null && setor.getId() != null) {
            return getId().equals(setor.getId());
        }
        return getNome() != null && getNome().equalsIgnoreCase(setor.getNome());
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hashCode(getId()) : Objects.hashCode(getNome() != null ? getNome().toLowerCase() : null);
    }
}
