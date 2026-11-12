/*
 * Área de risco do UC03: delimitação de perigo com agentes, limites e EPIs obrigatórios de acesso.
 * As regras de cadastro e atualização (RN1) ficam em AreaRiscoValidador, na camada de validação
 * (#263). Esta classe compara versões para o histórico de alterações (#114).
 */
package br.edu.safeplace.backend.domain.area_risco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Delimitação de perigo vinculada a um Setor físico da planta (RF05 / UC03).
 *
 * <p>Invariante central do UC03 (RN1): toda área de risco cadastrada, independentemente do grau
 * de perigo, exige o vínculo dos EPIs obrigatórios para acesso. A regra é verificada pelo
 * {@code EpisObrigatoriosElo} antes de salvar o cadastro ou a atualização (issue #263).</p>
 */
@Entity
@Table(name = "areas_risco")
public class AreaRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_perigo", nullable = false, length = 20)
    private NivelPerigo nivelPerigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @Convert(converter = AgentesRiscoConverter.class)
    @Column(name = "agentes_risco", columnDefinition = "text")
    private List<String> agentesRisco = Collections.emptyList();

    @Column(name = "limites", columnDefinition = "text")
    private String limites;

    /** EPIs obrigatórios de acesso (UC03), pela tabela de junção areas_risco_epis (#258). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "areas_risco_epis", joinColumns = @JoinColumn(name = "area_risco_id"),
            inverseJoinColumns = @JoinColumn(name = "epi_id"))
    @OrderBy("codigoEPI ASC")
    private Set<Epi> episObrigatorios = new LinkedHashSet<>();

    protected AreaRisco() {
    }

    public AreaRisco(
            Integer id,
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            Setor setor,
            List<String> agentesRisco,
            String limites,
            List<Epi> episObrigatorios) {
        this.id = id;
        this.codigo = codigo != null && !codigo.isBlank() ? codigo.trim().toUpperCase() : null;
        this.nome = nome != null ? nome.trim() : null;
        this.descricao = descricao != null ? descricao.trim() : null;
        this.nivelPerigo = nivelPerigo;
        this.setor = setor;
        this.agentesRisco = agentesRisco != null ? List.copyOf(agentesRisco) : Collections.emptyList();
        this.limites = limites != null ? limites.trim() : null;
        definirEpisObrigatorios(episObrigatorios);
    }

    public AreaRisco(
            Integer id,
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            List<Epi> episObrigatorios) {
        this(id, codigo, nome, descricao, nivelPerigo, null, Collections.emptyList(), null, episObrigatorios);
    }

    /** Cadastro de uma nova área de risco (UC03, cenário principal); validada por AreaRiscoValidador. */
    public static AreaRisco cadastrar(
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            Setor setor,
            List<String> agentesRisco,
            String limites,
            List<Epi> episObrigatorios) {
        return new AreaRisco(null, codigo, nome, descricao, nivelPerigo, setor, agentesRisco, limites, episObrigatorios);
    }

    public static AreaRisco cadastrar(
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            List<Epi> episObrigatorios) {
        return cadastrar(codigo, nome, descricao, nivelPerigo, null, List.of("Risco geral"), "Área demarcada", episObrigatorios);
    }

    /** Aplica alterações preservando o código identificador; validada por AreaRiscoValidador (#263). */
    public AreaRisco atualizar(
            String novoNome,
            String novaDescricao,
            NivelPerigo novoNivel,
            Setor novoSetor,
            List<String> novosAgentes,
            String novosLimites,
            List<Epi> novosEpis) {
        return new AreaRisco(
                this.id,
                this.codigo,
                novoNome,
                novaDescricao,
                novoNivel,
                novoSetor != null ? novoSetor : this.setor,
                novosAgentes,
                novosLimites,
                novosEpis);
    }

    /**
     * UC03, cenário alternativo II, passo 4 ("registrar nova versão no histórico"), e US05, regra 4:
     * compara esta versão com a {@code atualizada} e devolve cada campo que mudou, com o valor
     * anterior e o novo. Sem isso, a atualização sobrescreve a linha e o valor antigo se perde.
     *
     * @param descreverSetor função opcional de resolução de nome do setor a partir do id
     */
    public List<AlteracaoAreaRisco> compararCom(AreaRisco atualizada, Function<Integer, String> descreverSetor) {
        List<AlteracaoAreaRisco> alteracoes = new ArrayList<>();
        registrarSeMudou(alteracoes, "nome", getNome(), atualizada.getNome());
        registrarSeMudou(alteracoes, "descrição", getDescricao(), atualizada.getDescricao());
        registrarSeMudou(alteracoes, "nível de perigo", nomeDoNivel(getNivelPerigo()), nomeDoNivel(atualizada.getNivelPerigo()));
        if (!Objects.equals(getSetor(), atualizada.getSetor())) {
            alteracoes.add(new AlteracaoAreaRisco("setor",
                    descreverSetorOuNulo(getSetor(), descreverSetor),
                    descreverSetorOuNulo(atualizada.getSetor(), descreverSetor)));
        }
        registrarSeMudou(alteracoes, "agentes de risco", juntar(getAgentesRisco()), juntar(atualizada.getAgentesRisco()));
        registrarSeMudou(alteracoes, "limites", getLimites(), atualizada.getLimites());
        if (!idsDosEpis().equals(atualizada.idsDosEpis())) {
            alteracoes.add(new AlteracaoAreaRisco("EPIs obrigatórios", nomesDosEpis(), atualizada.nomesDosEpis()));
        }
        return alteracoes;
    }

    public List<AlteracaoAreaRisco> compararCom(AreaRisco atualizada) {
        return compararCom(atualizada, null);
    }

    private static void registrarSeMudou(List<AlteracaoAreaRisco> alteracoes, String campo, String anterior, String novo) {
        if (!Objects.equals(anterior, novo)) {
            alteracoes.add(new AlteracaoAreaRisco(campo, anterior, novo));
        }
    }

    private static String nomeDoNivel(NivelPerigo nivel) {
        return nivel != null ? nivel.name() : null;
    }

    private static String descreverSetorOuNulo(Setor setor, Function<Integer, String> descreverSetor) {
        if (setor == null) {
            return null;
        }
        if (setor.getNome() != null && !setor.getNome().isBlank()) {
            return setor.getNome();
        }
        return descreverSetor != null && setor.getId() != null ? descreverSetor.apply(setor.getId()) : null;
    }

    private static String juntar(List<String> valores) {
        return valores == null || valores.isEmpty() ? null : String.join(", ", valores);
    }

    private Set<Integer> idsDosEpis() {
        return episObrigatorios.stream().map(Epi::getId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private String nomesDosEpis() {
        return episObrigatorios.isEmpty() ? null
                : episObrigatorios.stream().map(Epi::getNome).collect(Collectors.joining(", "));
    }

    /** Substitui os EPIs obrigatórios de acesso, na ordem informada. */
    public void definirEpisObrigatorios(List<Epi> epis) {
        this.episObrigatorios = epis != null
                ? epis.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new))
                : new LinkedHashSet<>();
    }

    public Set<Integer> getEpisObrigatoriosIds() {
        return episObrigatorios.stream().map(Epi::getId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Operação {@code verificarEPIsObrigatoriosArea()} do diagrama de classes.
     */
    public List<Epi> verificarEpisObrigatoriosArea() {
        return List.copyOf(episObrigatorios);
    }

    public boolean exigeEpi(Integer epiId) {
        if (epiId == null) {
            return false;
        }
        return episObrigatorios.stream().anyMatch(epi -> epiId.equals(epi.getId()));
    }

    public boolean isCadastrada() {
        return codigo != null;
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

    public NivelPerigo getNivelPerigo() {
        return nivelPerigo;
    }

    public Setor getSetor() {
        return setor;
    }

    public List<String> getAgentesRisco() {
        return agentesRisco != null ? agentesRisco : Collections.emptyList();
    }

    public String getLimites() {
        return limites;
    }

    public List<Epi> getEpisObrigatorios() {
        return List.copyOf(episObrigatorios);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof AreaRisco area)) {
            return false;
        }
        if (getCodigo() == null || area.getCodigo() == null) {
            return false;
        }
        return getCodigo().equals(area.getCodigo());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCodigo());
    }

    @Converter
    static class AgentesRiscoConverter implements AttributeConverter<List<String>, String> {

        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            return String.join(", ", attribute);
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) {
                return List.of();
            }
            return Arrays.stream(dbData.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }
}
