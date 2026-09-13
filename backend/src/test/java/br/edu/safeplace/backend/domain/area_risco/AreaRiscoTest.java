package br.edu.safeplace.backend.domain.area_risco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AreaRiscoTest {

    @Test
    @DisplayName("Deve instanciar AreaRisco com dados válidos")
    void deveInstanciarAreaRiscoComDadosValidos() {
        AreaRisco area = new AreaRisco(1, "Linha de Produção", "Setor de solda e montagem", "ALTO");

        assertThat(area.getId()).isEqualTo(1);
        assertThat(area.getNome()).isEqualTo("Linha de Produção");
        assertThat(area.getDescricao()).isEqualTo("Setor de solda e montagem");
        assertThat(area.getNivelRisco()).isEqualTo("ALTO");
    }

    @Test
    @DisplayName("Deve criar nova AreaRisco sem ID através de método de fábrica")
    void deveCriarNovaAreaRiscoComMetodoFabrica() {
        AreaRisco area = AreaRisco.novo("Caldeiras", "Setor de alta pressão", "CRITICO");

        assertThat(area.getId()).isNull();
        assertThat(area.getNome()).isEqualTo("Caldeiras");
        assertThat(area.getDescricao()).isEqualTo("Setor de alta pressão");
        assertThat(area.getNivelRisco()).isEqualTo("CRITICO");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void deveRejeitarNomeNuloOuEmBranco(String nomeInvalido) {
        assertThatThrownBy(() -> new AreaRisco(1, nomeInvalido, "Descricao", "MEDIO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da área de risco é obrigatório.");
    }
}
