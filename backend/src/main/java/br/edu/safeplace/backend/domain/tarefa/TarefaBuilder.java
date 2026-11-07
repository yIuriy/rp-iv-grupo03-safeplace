package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Builder da entidade Tarefa (GoF, criacional, #240).
 * Separa a construção de Tarefa da sua representação, permitindo compor
 * identificador, descrição, classificação, área de risco, setores e EPIs
 * obrigatórios de forma fluente e nomeada.
 *
 * <p>O Builder não valida: a validação roda na camada validacao (TarefaValidador).
 */
public class TarefaBuilder {

    private Integer id;
    private String descricao;
    private NivelPerigo nivelPerigo;
    private LocalDateTime dataClassificacao;
    private GestorDeSeguranca responsavelClassificacao;
    private AreaRisco areaRisco;
    private List<Setor> setores = new ArrayList<>();
    private List<Epi> episObrigatorios = new ArrayList<>();

    public TarefaBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public TarefaBuilder descricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public TarefaBuilder nivelPerigo(NivelPerigo nivelPerigo) {
        this.nivelPerigo = nivelPerigo;
        return this;
    }

    public TarefaBuilder dataClassificacao(LocalDateTime dataClassificacao) {
        this.dataClassificacao = dataClassificacao;
        return this;
    }

    public TarefaBuilder responsavelClassificacao(GestorDeSeguranca responsavelClassificacao) {
        this.responsavelClassificacao = responsavelClassificacao;
        return this;
    }

    public TarefaBuilder areaRisco(AreaRisco areaRisco) {
        this.areaRisco = areaRisco;
        return this;
    }

    public TarefaBuilder setores(List<Setor> setores) {
        this.setores = setores != null ? new ArrayList<>(setores) : new ArrayList<>();
        return this;
    }

    public TarefaBuilder episObrigatorios(List<Epi> episObrigatorios) {
        this.episObrigatorios = episObrigatorios != null ? new ArrayList<>(episObrigatorios) : new ArrayList<>();
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public NivelPerigo getNivelPerigo() {
        return nivelPerigo;
    }

    public LocalDateTime getDataClassificacao() {
        return dataClassificacao;
    }

    public GestorDeSeguranca getResponsavelClassificacao() {
        return responsavelClassificacao;
    }

    public AreaRisco getAreaRisco() {
        return areaRisco;
    }

    public List<Setor> getSetores() {
        return setores;
    }

    public List<Epi> getEpisObrigatorios() {
        return episObrigatorios;
    }

    public Tarefa build() {
        return new Tarefa(this);
    }
}
