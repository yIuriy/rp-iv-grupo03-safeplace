package br.edu.safeplace.backend.validacao.setor;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/** Os casos de recusa vieram de {@code SetorTest} com as mesmas asserções (issue #263). */
@DisplayName("SetorValidador - cadeia de validação do setor (Chain of Responsibility)")
class SetorValidadorTest {

    private final SetorValidador validador = new SetorValidador();

    @Test
    @DisplayName("Aceita setor com nome e status")
    void aceitaSetorValido() {
        assertThatCode(() -> validador.validar(Setor.novo("Galpão B", "Desc", "Loc", NivelPerigo.MEDIO)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar criação de setor com nome nulo ou em branco")
    void deveRejeitarNomeInvalido() {
        assertThatThrownBy(() -> validador.validar(Setor.novo(null, "Desc", "Loc", NivelPerigo.MEDIO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do setor é obrigatório");

        assertThatThrownBy(() -> validador.validar(Setor.novo("   ", "Desc", "Loc", NivelPerigo.MEDIO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do setor é obrigatório");
    }

    @Test
    @DisplayName("Deve rejeitar status nulo")
    void deveRejeitarStatusNulo() {
        assertThatThrownBy(() -> validador.validar(Setor.novo("Galpão B", "Desc", "Loc", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status/Nível de perigo do setor é obrigatório");
    }
}
