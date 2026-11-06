package br.edu.safeplace.backend.validacao.tarefa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

/** A existência da área e dos setores é conferida pelo caso de uso ao resolvê-los (#260). */
@DisplayName("TarefaValidador - cadeia de validação da tarefa (Chain of Responsibility)")
class TarefaValidadorTest {

    private final TarefaValidador validador = new TarefaValidador();

    @Test
    @DisplayName("Aceita tarefa com descrição, área e setores")
    void aceitaTarefaValida() {
        Tarefa tarefa = Tarefa.cadastrarComRelacoes("Inspeção visual", mock(AreaRisco.class), List.of(mock(Setor.class)));

        assertThatCode(() -> validador.validar(tarefa)).doesNotThrowAnyException();
        assertThat(validador.verificar(tarefa).valido()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Rejeita descrição nula ou em branco (regra que saiu do construtor de Tarefa)")
    void rejeitaDescricaoNulaOuEmBranco(String descricaoInvalida) {
        Tarefa tarefa = Tarefa.cadastrar(descricaoInvalida);

        assertThatThrownBy(() -> validador.validar(tarefa))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Descrição da tarefa é obrigatória.");
    }
}
