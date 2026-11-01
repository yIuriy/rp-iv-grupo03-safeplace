/*
 * Área de risco do UC03: delimitação de perigo com agentes, limites e EPIs obrigatórios de acesso.
 * As regras de cadastro e atualização (RN1) ficam em AreaRiscoValidador, na camada de validação
 * (#263). Esta classe compara versões para o histórico de alterações (#114).
 */
package br.edu.safeplace.backend.domain.area_risco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;

/**
 * Delimitação de perigo vinculada a um Setor físico da planta (RF05 / UC03).
 *
 * <p>Invariante central do UC03 (RN1): toda área de risco cadastrada, independentemente do grau
 * de perigo, exige o vínculo dos EPIs obrigatórios para acesso. A regra é verificada pelo
 * {@code EpisObrigatoriosElo} antes de salvar o cadastro ou a atualização (issue #263).</p>
 */
public class AreaRisco {

    private final Integer id;
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final NivelPerigo nivelPerigo;
    private final Integer setorId;
    private final List<String> agentesRisco;
    private final String limites;
    private final List<Epi> episObrigatorios;

    public AreaRisco(
            Integer id,
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            Integer setorId,
            List<String> agentesRisco,
            String limites,
            List<Epi> episObrigatorios) {
        this.id = id;
        this.codigo = codigo != null && !codigo.isBlank() ? codigo.trim().toUpperCase() : null;
        this.nome = nome != null ? nome.trim() : null;
        this.descricao = descricao != null ? descricao.trim() : null;
        this.nivelPerigo = nivelPerigo;
        this.setorId = setorId;
        this.agentesRisco = agentesRisco != null ? List.copyOf(agentesRisco) : Collections.emptyList();
        this.limites = limites != null ? limites.trim() : null;
        this.episObrigatorios = episObrigatorios != null ? List.copyOf(episObrigatorios) : Collections.emptyList();
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
            Integer setorId,
            List<String> agentesRisco,
            String limites,
            List<Epi> episObrigatorios) {
        return new AreaRisco(null, codigo, nome, descricao, nivelPerigo, setorId, agentesRisco, limites, episObrigatorios);
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
            Integer novoSetorId,
            List<String> novosAgentes,
            String novosLimites,
            List<Epi> novosEpis) {
        return new AreaRisco(
                this.id,
                this.codigo,
                novoNome,
                novaDescricao,
                novoNivel,
                novoSetorId != null ? novoSetorId : this.setorId,
                novosAgentes,
                novosLimites,
                novosEpis);
    }

    /**
     * UC03, cenário alternativo II, passo 4 ("registrar nova versão no histórico"), e US05, regra 4:
     * compara esta versão com a {@code atualizada} e devolve cada campo que mudou, com o valor
     * anterior e o novo. Sem isso, a atualização sobrescreve a linha e o valor antigo se perde.
     *
     * @param descreverSetor nome do setor a partir do id; a área guarda só o id do {@link Setor}
     */
    public List<AlteracaoAreaRisco> compararCom(AreaRisco atualizada, Function<Integer, String> descreverSetor) {
        List<AlteracaoAreaRisco> alteracoes = new ArrayList<>();
        registrarSeMudou(alteracoes, "nome", nome, atualizada.nome);
        registrarSeMudou(alteracoes, "descrição", descricao, atualizada.descricao);
        registrarSeMudou(alteracoes, "nível de perigo", nomeDoNivel(nivelPerigo), nomeDoNivel(atualizada.nivelPerigo));
        if (!Objects.equals(setorId, atualizada.setorId)) {
            alteracoes.add(new AlteracaoAreaRisco("setor",
                    descreverSetorOuNulo(setorId, descreverSetor),
                    descreverSetorOuNulo(atualizada.setorId, descreverSetor)));
        }
        registrarSeMudou(alteracoes, "agentes de risco", juntar(agentesRisco), juntar(atualizada.agentesRisco));
        registrarSeMudou(alteracoes, "limites", limites, atualizada.limites);
        if (!idsDosEpis().equals(atualizada.idsDosEpis())) {
            alteracoes.add(new AlteracaoAreaRisco("EPIs obrigatórios", nomesDosEpis(), atualizada.nomesDosEpis()));
        }
        return alteracoes;
    }

    private static void registrarSeMudou(List<AlteracaoAreaRisco> alteracoes, String campo, String anterior, String novo) {
        if (!Objects.equals(anterior, novo)) {
            alteracoes.add(new AlteracaoAreaRisco(campo, anterior, novo));
        }
    }

    private static String nomeDoNivel(NivelPerigo nivel) {
        return nivel != null ? nivel.name() : null;
    }

    private static String descreverSetorOuNulo(Integer id, Function<Integer, String> descreverSetor) {
        return id != null ? descreverSetor.apply(id) : null;
    }

    private static String juntar(List<String> valores) {
        return valores.isEmpty() ? null : String.join(", ", valores);
    }

    private Set<Integer> idsDosEpis() {
        return episObrigatorios.stream().map(Epi::getId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private String nomesDosEpis() {
        return episObrigatorios.isEmpty() ? null
                : episObrigatorios.stream().map(Epi::getNome).collect(Collectors.joining(", "));
    }

    /**
     * Operação {@code verificarEPIsObrigatoriosArea()} do diagrama de classes.
     */
    public List<Epi> verificarEpisObrigatoriosArea() {
        return episObrigatorios;
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

    public Integer getSetorId() {
        return setorId;
    }

    public List<String> getAgentesRisco() {
        return agentesRisco;
    }

    public String getLimites() {
        return limites;
    }

    public List<Epi> getEpisObrigatorios() {
        return episObrigatorios;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof AreaRisco area)) {
            return false;
        }
        if (codigo == null || area.codigo == null) {
            return false;
        }
        return codigo.equals(area.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
