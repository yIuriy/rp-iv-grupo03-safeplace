package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreinamentoTest {

    @Test
    @DisplayName("Deve instanciar treinamento coletivo com sucesso")
    void deveInstanciarTreinamentoComSucesso() {
        LocalDate hoje = LocalDate.now();
        Treinamento treinamento = Treinamento.cadastrar(
                "Integração de Segurança",
                "Normas internas e procedimentos de emergência",
                "Carlos Instrutor",
                hoje.minusDays(2),
                8,
                List.of(1, 2, 3)
        );

        assertThat(treinamento.getId()).isNull();
        assertThat(treinamento.getNome()).isEqualTo("Integração de Segurança");
        assertThat(treinamento.getInstrutor()).isEqualTo("Carlos Instrutor");
        assertThat(treinamento.getCargaHoraria()).isEqualTo(8);
        assertThat(treinamento.getColaboradoresIds()).containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void deveRejeitarNomeInvalido(String nomeInvalido) {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> Treinamento.cadastrar(nomeInvalido, "Conteúdo", "Instrutor", hoje, 4, List.of(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome do treinamento é obrigatório.");
    }

    @Test
    @DisplayName("Deve rejeitar data de realização no futuro")
    void deveRejeitarDataNoFuturo() {
        LocalDate futuro = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", futuro, 4, List.of(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de realização não pode ser no futuro.");
    }

    @Test
    @DisplayName("Deve rejeitar treinamento sem nenhum colaborador")
    void deveRejeitarSemColaborador() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", hoje, 4, Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Treinamento deve possuir ao menos um colaborador vinculado.");

        assertThatThrownBy(() -> Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", hoje, 4, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Treinamento deve possuir ao menos um colaborador vinculado.");
    }
}
