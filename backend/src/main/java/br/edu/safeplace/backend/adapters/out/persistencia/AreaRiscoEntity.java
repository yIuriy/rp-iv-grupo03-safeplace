package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "areas_risco")
public class AreaRiscoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "nivel_perigo", nullable = false, length = 20)
    private String nivelPerigo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "areas_risco_epis", joinColumns = @JoinColumn(name = "area_risco_id"), inverseJoinColumns = @JoinColumn(name = "epi_id"))
    private List<EpiEntity> episObrigatorios = new ArrayList<>();

    public AreaRiscoEntity() {
    }

    public AreaRiscoEntity(Integer id, String codigo, String nome, String descricao, String nivelPerigo,
            List<EpiEntity> episObrigatorios) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.nivelPerigo = nivelPerigo;
        this.episObrigatorios = episObrigatorios != null ? new ArrayList<>(episObrigatorios) : new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNivelPerigo() {
        return nivelPerigo;
    }

    public List<EpiEntity> getEpisObrigatorios() {
        return episObrigatorios;
    }
}
