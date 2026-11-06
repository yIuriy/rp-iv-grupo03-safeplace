package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Atividade classificada pela matriz de periculosidade (RF06 / UC11).
 *
 * <p>O atributo {@code status : NivelPerigo} do diagrama de classes corresponde a
 * {@link #getNivelPerigo()}. A classificação usa o vocabulário único de {@link NivelPerigo}, o mesmo
 * de {@code AreaRisco} e {@code Setor}; a correspondência com os termos de UC11 e US06 está em
 * {@code docs/arquitetura/correspondencia-classes-tarefa-uc11.md} (issue #112).</p>
 *
 * <p>Uma tarefa pode existir sem classificação: o cenário de exceção I do UC11 prevê justamente a
 * tentativa de alocar um colaborador em atividade "sem grau de risco cadastrado". Por isso o nível
 * é opcional no cadastro e a proteção fica no {@code TarefaClassificadaElo} da camada de validação
 * (issue #228). Uma vez
 * classificada, a tarefa não volta a ficar sem grau de risco.</p>
 *
 * <p>O vínculo entre tarefa e EPIs ({@code associarEPIs} e {@code verificarEPIsObrigatoriosTarefa}
 * no diagrama de classes) atende ao RF12 e UC08 (backlog), vinculando os equipamentos
 * obrigatórios para a execução da atividade, separado dos EPIs de acesso à área (UC03).</p>
 *
 * <p>É a entidade JPA da tabela tarefas (#260): área de risco, setores e EPIs são associações por objeto.</p>
 */
@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_perigo", length = 20)
    private NivelPerigo nivelPerigo;

    @Column(name = "data_classificacao")
    private LocalDateTime dataClassificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_classificacao_id")
    private GestorDeSeguranca responsavelClassificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_risco_id")
    private AreaRisco areaRisco;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tarefas_setores", joinColumns = @JoinColumn(name = "tarefa_id"),
            inverseJoinColumns = @JoinColumn(name = "setor_id"))
    private List<Setor> setores = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tarefas_epis", joinColumns = @JoinColumn(name = "tarefa_id"),
            inverseJoinColumns = @JoinColumn(name = "epi_id"))
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<Epi> episObrigatorios = new ArrayList<>();

    /** Exclusivo do JPA (decisão #254). */
    protected Tarefa() {
    }

    /**
     * Reconstitui uma tarefa gravada. {@code responsavelClassificacao} é nulo em classificações
     * anteriores ao registro de autoria (#112), que não recebem autor inventado.
     */
    public Tarefa(Integer id, String descricao, NivelPerigo nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSeguranca responsavelClassificacao) {
        this(id, descricao, nivelPerigo, dataClassificacao, responsavelClassificacao, null, List.of(), List.of());
    }

    public Tarefa(Integer id, String descricao, NivelPerigo nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSeguranca responsavelClassificacao, AreaRisco areaRisco, List<Setor> setores) {
        this(id, descricao, nivelPerigo, dataClassificacao, responsavelClassificacao, areaRisco, setores, List.of());
    }

    public Tarefa(Integer id, String descricao, NivelPerigo nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSeguranca responsavelClassificacao, AreaRisco areaRisco, List<Setor> setores,
            List<Epi> episObrigatorios) {
        // A descrição é validada em TarefaValidador, antes do repository (issue #228).
        this.id = id;
        this.descricao = descricao != null ? descricao.trim() : null;
        this.nivelPerigo = nivelPerigo;
        this.dataClassificacao = nivelPerigo != null && dataClassificacao == null
                ? LocalDateTime.now()
                : dataClassificacao;
        this.responsavelClassificacao = nivelPerigo != null ? responsavelClassificacao : null;
        this.areaRisco = areaRisco;
        this.setores = setores == null ? new ArrayList<>() : new ArrayList<>(setores.stream().distinct().toList());
        this.episObrigatorios = episObrigatorios != null ? new ArrayList<>(episObrigatorios) : new ArrayList<>();
    }

    public Tarefa(Integer id, String descricao, NivelPerigo nivelPerigo, LocalDateTime dataClassificacao) {
        this(id, descricao, nivelPerigo, dataClassificacao, null);
    }

    /**
     * Cadastro de uma tarefa ainda sem classificação; o Gestor de Segurança a classifica depois,
     * conforme o cenário principal do UC11.
     */
    public static Tarefa cadastrar(String descricao) {
        return new Tarefa(null, descricao, null, null, null);
    }

    public static Tarefa cadastrarComRelacoes(String descricao, AreaRisco areaRisco, List<Setor> setores) {
        return new Tarefa(null, descricao, null, null, null, areaRisco, setores);
    }

    public Tarefa atualizar(String novaDescricao, AreaRisco novaAreaRisco, List<Setor> novosSetores) {
        return new Tarefa(
                this.id,
                novaDescricao,
                this.nivelPerigo,
                this.dataClassificacao,
                this.responsavelClassificacao,
                novaAreaRisco,
                novosSetores,
                this.episObrigatorios);
    }

    /**
     * Cadastro com classificação imediata. O nível é opcional; quando informado, exige o
     * responsável técnico, como qualquer outra classificação.
     */
    public static Tarefa cadastrar(String descricao, NivelPerigo nivelPerigo, GestorDeSeguranca responsavel) {
        Tarefa tarefa = cadastrar(descricao);
        if (nivelPerigo != null) {
            tarefa.classificar(nivelPerigo, responsavel);
        }
        return tarefa;
    }

    /**
     * Define ou reavalia o grau de risco da atividade (UC11, cenário principal e alternativo I),
     * registrando data, hora e responsável técnico (regra 1). Reavaliar para o mesmo nível é
     * aceito e também registrado, pois confirma a avaliação periódica.
     *
     * @return o registro a preservar no histórico, com o nível anterior e o novo
     */
    public RegistroClassificacaoTarefa classificar(NivelPerigo novoNivel, GestorDeSeguranca responsavel) {
        if (novoNivel == null) {
            throw new IllegalArgumentException("Nível de perigo da tarefa é obrigatório.");
        }
        if (responsavel == null) {
            throw new IllegalArgumentException("Responsável técnico pela classificação é obrigatório.");
        }
        NivelPerigo anterior = this.nivelPerigo;
        this.nivelPerigo = novoNivel;
        this.dataClassificacao = LocalDateTime.now();
        this.responsavelClassificacao = responsavel;
        return new RegistroClassificacaoTarefa(id, anterior, novoNivel, responsavel, dataClassificacao);
    }

    public boolean estaClassificada() {
        return nivelPerigo != null;
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
        return Collections.unmodifiableList(setores);
    }

    public Integer getAreaRiscoId() {
        return areaRisco != null ? areaRisco.getId() : null;
    }

    public List<Integer> getSetoresIds() {
        return setores.stream().map(Setor::getId).toList();
    }

    /**
     * Operação {@code associarEPIs} do diagrama de classes (RF12 / UC08).
     * Vincula a lista de EPIs obrigatórios para a realização da tarefa.
     */
    public boolean associarEpis(List<Epi> novosEpis) {
        this.episObrigatorios = novosEpis != null ? new ArrayList<>(novosEpis) : new ArrayList<>();
        return true;
    }

    public boolean associarEPIs(List<Epi> novosEpis) {
        return associarEpis(novosEpis);
    }

    /**
     * Operação {@code verificarEPIsObrigatoriosTarefa()} do diagrama de classes (RF12 / UC08).
     * Retorna a lista de EPIs obrigatórios vinculados a esta tarefa.
     */
    public List<Epi> verificarEpisObrigatoriosTarefa() {
        return Collections.unmodifiableList(episObrigatorios);
    }

    public List<Epi> verificarEPIsObrigatoriosTarefa() {
        return verificarEpisObrigatoriosTarefa();
    }

    public List<Epi> getEpisObrigatorios() {
        return Collections.unmodifiableList(episObrigatorios);
    }

    /**
     * Verifica se determinado EPI é exigido para a tarefa.
     */
    public boolean exigeEpi(Integer epiId) {
        if (epiId == null || episObrigatorios == null) {
            return false;
        }
        return episObrigatorios.stream().anyMatch(epi -> epiId.equals(epi.getId()));
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Tarefa tarefa)) {
            return false;
        }
        return getId() != null && getId().equals(tarefa.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
