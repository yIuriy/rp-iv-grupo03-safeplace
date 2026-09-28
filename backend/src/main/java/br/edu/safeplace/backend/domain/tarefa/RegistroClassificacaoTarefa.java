package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Alteração do grau de risco de uma tarefa (UC11, regra 1; US06, critério 2). Não é entidade nova:
 * é o dado que o caso de uso grava na trilha de {@code LogAuditoria}, preservando o valor anterior,
 * o novo, o momento e o responsável técnico. {@code nivelAnterior} é nulo na primeira classificação.
 */
public record RegistroClassificacaoTarefa(
        Integer idTarefa,
        NivelPerigo nivelAnterior,
        NivelPerigo nivelNovo,
        GestorDeSeguranca responsavel,
        LocalDateTime dataHora) {
}
