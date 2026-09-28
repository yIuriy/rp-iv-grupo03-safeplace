package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;

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

    public TarefaEntity() {
    }

    public TarefaEntity(Integer id, String descricao, String nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSegurancaEntidade responsavelClassificacao) {
        this.id = id;
        this.descricao = descricao;
        this.nivelPerigo = nivelPerigo;
        this.dataClassificacao = dataClassificacao;
        this.responsavelClassificacao = responsavelClassificacao;
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
}
