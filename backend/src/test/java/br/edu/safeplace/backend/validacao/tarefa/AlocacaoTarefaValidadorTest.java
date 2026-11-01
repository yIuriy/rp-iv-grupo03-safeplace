package br.edu.safeplace.backend.validacao.tarefa;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

@DisplayName("AlocacaoTarefaValidador - registro da alocação (Chain of Responsibility)")
class AlocacaoTarefaValidadorTest {

    private final AlocacaoTarefaValidador validador = new AlocacaoTarefaValidador();

    @Test
    @DisplayName("Aceita alocação com tarefa, colaborador e responsável")
    void aceitaAlocacaoValida() {
        assertThatCode(() -> validador.validar(AlocacaoTarefa.nova(1, 2, 3, null))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Recusa alocação sem tarefa, sem colaborador ou sem responsável (regras que estavam no construtor)")
    void recusaCamposAusentes() {
        assertThatThrownBy(() -> validador.validar(AlocacaoTarefa.nova(null, 2, 3, null)))
                .hasMessage("Identificador da tarefa é obrigatório para alocação.");
        assertThatThrownBy(() -> validador.validar(AlocacaoTarefa.nova(1, null, 3, null)))
                .hasMessage("Identificador do colaborador é obrigatório para alocação.");
        assertThatThrownBy(() -> validador.validar(AlocacaoTarefa.nova(1, 2, null, null)))
                .hasMessage("Responsável pelo registro da alocação é obrigatório.");
    }
}
