package br.edu.safeplace.backend.domain.area_risco;

import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;

/**
 * Setor físico mapeado como área de risco (RF05 / UC03).
 *
 * <p>Invariante central do UC03 (RN1): toda área de risco cadastrada, independentemente do grau
 * de perigo, exige o vínculo dos EPIs obrigatórios para acesso. A regra é aplicada na fábrica
 * {@link #cadastrar}, que é o único caminho de entrada do caso de uso de cadastro.</p>
 */
public class AreaRisco {

    private final Integer id;
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final NivelPerigo nivelPerigo;
    private final List<Epi> episObrigatorios;

    public AreaRisco(
            Integer id,
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            List<Epi> episObrigatorios) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da área de risco é obrigatório.");
        }

        this.id = id;
        this.codigo = codigo != null && !codigo.isBlank() ? codigo.trim().toUpperCase() : null;
        this.nome = nome.trim();
        this.descricao = descricao != null ? descricao.trim() : null;
        this.nivelPerigo = nivelPerigo;
        this.episObrigatorios = episObrigatorios != null ? List.copyOf(episObrigatorios) : List.of();
    }

    /**
     * Cadastro de uma nova área de risco (UC03, cenário principal).
     *
     * @throws AreaRiscoSemEpiObrigatorioException quando nenhum EPI de acesso é vinculado (UC03,
     *                                             RN1 e cenário de exceção II)
     */
    public static AreaRisco cadastrar(
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
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

        return new AreaRisco(null, codigo, nome, descricao, nivelPerigo, episObrigatorios);
    }

    /**
     * Referência de área apenas nominal, sem cadastro próprio.
     *
     * <p>Usada pelo construtor de compatibilidade de {@code Ocorrencia}, que ainda deriva a área
     * a partir do campo textual {@code local}. Não representa uma área cadastrada por UC03 e por
     * isso não passa pela validação de EPIs obrigatórios.</p>
     */
    public static AreaRisco novo(String nome, String descricao, NivelPerigo nivelPerigo) {
        return new AreaRisco(null, null, nome, descricao, nivelPerigo, List.of());
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

    /**
     * Indica se esta área está cadastrada como setor do mapa de riscos (UC03) ou se é apenas uma
     * referência nominal derivada do local de uma ocorrência.
     */
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
