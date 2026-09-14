package br.edu.safeplace.backend.domain.tarefa.exception;

/**
 * UC11, cenário de exceção I: função sem nível de periculosidade atribuído.
 */
public class TarefaSemClassificacaoException extends RuntimeException {

    public TarefaSemClassificacaoException(String descricao) {
        super("A tarefa \"" + descricao
                + "\" ainda não possui nível de periculosidade classificado.");
    }
}
