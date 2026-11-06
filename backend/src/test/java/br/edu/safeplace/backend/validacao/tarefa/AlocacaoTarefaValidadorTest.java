package br.edu.safeplace.backend.validacao.tarefa;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@DisplayName("AlocacaoTarefaValidador - registro da alocação (Chain of Responsibility)")
class AlocacaoTarefaValidadorTest {

    private final AlocacaoTarefaValidador validador = new AlocacaoTarefaValidador();
    private final Tarefa tarefa = new Tarefa(1, "Solda", null, null);
    private final Colaborador colaborador = pessoa(2);
    private final Colaborador responsavel = pessoa(3);

    private static Colaborador pessoa(int id) {
        return new Colaborador(id, "52998224725", "Pessoa " + id, LocalDate.of(1990, 1, 1), null, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Aceita alocação com tarefa, colaborador e responsável")
    void aceitaAlocacaoValida() {
        assertThatCode(() -> validador.validar(AlocacaoTarefa.nova(tarefa, colaborador, responsavel, null))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Recusa alocação sem tarefa, sem colaborador ou sem responsável (regras que estavam no construtor)")
    void recusaCamposAusentes() {
        assertThatThrownBy(() -> validador.validar(AlocacaoTarefa.nova(null, colaborador, responsavel, null)))
                .hasMessage("Identificador da tarefa é obrigatório para alocação.");
        assertThatThrownBy(() -> validador.validar(AlocacaoTarefa.nova(tarefa, null, responsavel, null)))
                .hasMessage("Identificador do colaborador é obrigatório para alocação.");
        assertThatThrownBy(() -> validador.validar(AlocacaoTarefa.nova(tarefa, colaborador, null, null)))
                .hasMessage("Responsável pelo registro da alocação é obrigatório.");
    }
}
