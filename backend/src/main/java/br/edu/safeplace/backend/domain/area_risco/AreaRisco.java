package br.edu.safeplace.backend.domain.area_risco;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;

/**
 * Delimitação de perigo vinculada a um Setor físico da planta (RF05 / UC03).
 *
 * <p>Invariante central do UC03 (RN1): toda área de risco cadastrada, independentemente do grau
 * de perigo, exige o vínculo dos EPIs obrigatórios para acesso. A regra é validada na fábrica
 * {@link #cadastrar} e em qualquer atualização.</p>
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

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da área de risco é obrigatório.");
        }

        this.id = id;
        this.codigo = codigo != null && !codigo.isBlank() ? codigo.trim().toUpperCase() : null;
        this.nome = nome.trim();
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

    /**
     * Cadastro de uma nova área de risco (UC03, cenário principal).
     *
     * @throws AreaRiscoSemEpiObrigatorioException quando nenhum EPI de acesso é vinculado (UC03, RN1)
     */
    public static AreaRisco cadastrar(
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            Integer setorId,
            List<String> agentesRisco,
            String limites,
            List<Epi> episObrigatorios) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código da área de risco é obrigatório.");
        }
        if (nivelPerigo == null) {
            throw new IllegalArgumentException("Nível de perigo da área de risco é obrigatório.");
        }
        if (episObrigatorios == null || episObrigatorios.isEmpty()) {
            throw new AreaRiscoSemEpiObrigatorioException(codigo);
        }

        return new AreaRisco(null, codigo, nome, descricao, nivelPerigo, setorId, agentesRisco, limites, episObrigatorios);
    }

    public static AreaRisco cadastrar(
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            List<Epi> episObrigatorios) {
        return cadastrar(codigo, nome, descricao, nivelPerigo, null, Collections.emptyList(), null, episObrigatorios);
    }

    /**
     * Operação {@code validarDadosAreaRisco()} do diagrama de classes.
     */
    public boolean validarDadosAreaRisco() {
        if (codigo == null || codigo.isBlank()) return false;
        if (nome == null || nome.isBlank()) return false;
        if (nivelPerigo == null) return false;
        return episObrigatorios != null && !episObrigatorios.isEmpty();
    }

    /**
     * Operação {@code salvarAreaRisco()} do diagrama de classes.
     */
    public void salvarAreaRisco() {
        if (!validarDadosAreaRisco()) {
            throw new IllegalStateException("Dados da área de risco inválidos para salvar.");
        }
    }

    /**
     * Operação {@code validarAlteracoes()} do diagrama de classes.
     * Valida se a alteração mantém os dados consistentes e os EPIs obrigatórios exigidos por RN1.
     */
    public boolean validarAlteracoes(
            String novoNome,
            NivelPerigo novoNivel,
            List<Epi> novosEpis) {
        if (novoNome == null || novoNome.isBlank()) return false;
        if (novoNivel == null) return false;
        return novosEpis != null && !novosEpis.isEmpty();
    }

    /**
     * Aplica alterações na área de risco preservando o código identificador.
     */
    public AreaRisco atualizar(
            String novoNome,
            String novaDescricao,
            NivelPerigo novoNivel,
            Integer novoSetorId,
            List<String> novosAgentes,
            String novosLimites,
            List<Epi> novosEpis) {

        if (!validarAlteracoes(novoNome, novoNivel, novosEpis)) {
            if (novosEpis == null || novosEpis.isEmpty()) {
                throw new AreaRiscoSemEpiObrigatorioException(this.codigo);
            }
            throw new IllegalArgumentException("Dados de alteração da área de risco são inválidos.");
        }

        return new AreaRisco(
                this.id,
                this.codigo,
                novoNome,
                novaDescricao,
                novoNivel,
                novoSetorId != null ? novoSetorId : this.setorId,
                novosAgentes != null ? novosAgentes : this.agentesRisco,
                novosLimites != null ? novosLimites : this.limites,
                novosEpis);
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
