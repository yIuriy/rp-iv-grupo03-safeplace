package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

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
 * é opcional no cadastro e a proteção fica em {@link #validarClassificacaoParaAlocacao()}. Uma vez
 * classificada, a tarefa não volta a ficar sem grau de risco.</p>
 *
 * <p>O vínculo entre tarefa e EPIs ({@code associarEPIs} e {@code verificarEPIsObrigatoriosTarefa}
 * no diagrama de classes) pertence a RF12, que permanece no backlog e não faz parte deste módulo.</p>
 */
public class Tarefa {

    private final Integer id;
    private final String descricao;
    private NivelPerigo nivelPerigo;
    private LocalDateTime dataClassificacao;
    private GestorDeSeguranca responsavelClassificacao;

    /**
     * Reconstitui uma tarefa gravada. {@code responsavelClassificacao} é nulo em classificações
     * anteriores ao registro de autoria (#112), que não recebem autor inventado.
     */
    public Tarefa(Integer id, String descricao, NivelPerigo nivelPerigo, LocalDateTime dataClassificacao,
            GestorDeSeguranca responsavelClassificacao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da tarefa é obrigatória.");
        }

        this.id = id;
        this.descricao = descricao.trim();
        this.nivelPerigo = nivelPerigo;
        this.dataClassificacao = nivelPerigo != null && dataClassificacao == null
                ? LocalDateTime.now()
                : dataClassificacao;
        this.responsavelClassificacao = nivelPerigo != null ? responsavelClassificacao : null;
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

    /**
     * UC11, cenário de exceção I: bloqueia a alocação em atividade sem grau de risco cadastrado.
     * O ponto de alocação que chama esta verificação pertence a #117 e #126.
     */
    public void validarClassificacaoParaAlocacao() {
        if (!estaClassificada()) {
            throw new TarefaSemClassificacaoException(descricao);
        }
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
}
