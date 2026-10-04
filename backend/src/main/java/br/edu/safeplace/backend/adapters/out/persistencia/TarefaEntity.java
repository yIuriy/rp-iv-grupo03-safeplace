package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "tarefas")
public class TarefaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "nivel_perigo", length = 20)
    private String nivelPerigo;

    @Column(name = "data_classificacao")
    private LocalDateTime dataClassificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_classificacao_id")
    private GestorDeSegurancaEntidade responsavelClassificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_risco_id")
    private AreaRiscoEntity areaRisco;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tarefas_setores", joinColumns = @JoinColumn(name = "tarefa_id"),
            inverseJoinColumns = @JoinColumn(name = "setor_id"))
    private List<SetorEntity> setores = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tarefas_epis", joinColumns = @JoinColumn(name = "tarefa_id"),
            inverseJoinColumns = @JoinColumn(name = "epi_id"))
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<EpiEntity> epis = new ArrayList<>();

    public TarefaEntity() {
    }

    public TarefaEntity(Integer id, String descricao, String nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSegurancaEntidade responsavelClassificacao) {
        this(id, descricao, nivelPerigo, dataClassificacao, responsavelClassificacao, null, List.of(), List.of());
    }

    public TarefaEntity(Integer id, String descricao, String nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSegurancaEntidade responsavelClassificacao, AreaRiscoEntity areaRisco, List<SetorEntity> setores) {
        this(id, descricao, nivelPerigo, dataClassificacao, responsavelClassificacao, areaRisco, setores, List.of());
    }

    public TarefaEntity(Integer id, String descricao, String nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSegurancaEntidade responsavelClassificacao, AreaRiscoEntity areaRisco, List<SetorEntity> setores,
            List<EpiEntity> epis) {
        this.id = id;
        this.descricao = descricao;
        this.nivelPerigo = nivelPerigo;
        this.dataClassificacao = dataClassificacao;
        this.responsavelClassificacao = responsavelClassificacao;
        this.areaRisco = areaRisco;
        this.setores = setores != null ? new ArrayList<>(setores) : new ArrayList<>();
        this.epis = epis != null ? new ArrayList<>(epis) : new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNivelPerigo() {
        return nivelPerigo;
    }

    public LocalDateTime getDataClassificacao() {
        return dataClassificacao;
    }

    public GestorDeSegurancaEntidade getResponsavelClassificacao() {
        return responsavelClassificacao;
    }

    public AreaRiscoEntity getAreaRisco() {
        return areaRisco;
    }

    public List<SetorEntity> getSetores() {
        return setores;
    }

    public List<EpiEntity> getEpis() {
        return epis;
    }
}
