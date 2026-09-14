package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;

/**
 * Atividade classificada pela matriz de periculosidade (RF06 / UC11).
 *
 * <p>Uma tarefa pode existir sem classificação: o cenário de exceção I do UC11 prevê justamente a
 * tentativa de alocar um colaborador em atividade "sem grau de risco cadastrado". Por isso o nível
 * é opcional no cadastro e a proteção fica em {@link #validarClassificacaoParaAlocacao()}.</p>
 *
 * <p>O vínculo entre tarefa e EPIs ({@code associarEPIs} e {@code verificarEPIsObrigatoriosTarefa}
 * no diagrama de classes) pertence a RF12, que permanece no backlog e não faz parte deste módulo.</p>
 */
public class Tarefa {

    private final Integer id;
    private final String descricao;
    private NivelPerigo nivelPerigo;
    private LocalDateTime dataClassificacao;

    public Tarefa(Integer id, String descricao, NivelPerigo nivelPerigo, LocalDateTime dataClassificacao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da tarefa é obrigatória.");
        }

        this.id = id;
        this.descricao = descricao.trim();
        this.nivelPerigo = nivelPerigo;
        this.dataClassificacao = nivelPerigo != null && dataClassificacao == null
                ? LocalDateTime.now()
                : dataClassificacao;
    }

    /**
     * Cadastro de uma nova tarefa. O nível de perigo é opcional e pode ser definido depois pelo
     * Gestor de Segurança, conforme o cenário principal do UC11.
     */
    public static Tarefa cadastrar(String descricao, NivelPerigo nivelPerigo) {
        return new Tarefa(null, descricao, nivelPerigo, null);
    }

    /**
     * Define ou reavalia o grau de risco da atividade (UC11, cenário principal e alternativo I).
     *
     * <p>A data e a hora da alteração são registradas aqui, atendendo parcialmente a RN1 do UC11.
     * O registro do responsável técnico depende do módulo de auditoria, ainda não implementado.</p>
     */
    public void classificar(NivelPerigo novoNivel) {
        if (novoNivel == null) {
            throw new IllegalArgumentException("Nível de perigo da tarefa é obrigatório.");
        }
        this.nivelPerigo = novoNivel;
        this.dataClassificacao = LocalDateTime.now();
    }

    public boolean estaClassificada() {
        return nivelPerigo != null;
    }

    /**
     * UC11, cenário de exceção I: bloqueia a alocação em atividade sem grau de risco cadastrado.
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
}
