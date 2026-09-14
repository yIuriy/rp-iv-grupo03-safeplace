package br.edu.safeplace.backend.domain.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NivelPerigoTest {

    @Test
    @DisplayName("Deve expor os quatro graus do UC03 e do diagrama de classes")
    void deveExporOsQuatroGraus() {
        assertThat(NivelPerigo.values())
                .containsExactly(NivelPerigo.BAIXO, NivelPerigo.MEDIO, NivelPerigo.ALTO, NivelPerigo.CRITICO);
    }

    @Test
    @DisplayName("A gravidade deve crescer de BAIXO até CRITICO")
    void gravidadeDeveCrescer() {
        assertThat(NivelPerigo.BAIXO.getGravidade()).isLessThan(NivelPerigo.MEDIO.getGravidade());
        assertThat(NivelPerigo.MEDIO.getGravidade()).isLessThan(NivelPerigo.ALTO.getGravidade());
        assertThat(NivelPerigo.ALTO.getGravidade()).isLessThan(NivelPerigo.CRITICO.getGravidade());
    }

    @Test
    @DisplayName("Deve comparar graus de perigo")
    void deveCompararGrausDePerigo() {
        assertThat(NivelPerigo.CRITICO.isPeloMenos(NivelPerigo.ALTO)).isTrue();
        assertThat(NivelPerigo.ALTO.isPeloMenos(NivelPerigo.ALTO)).isTrue();
        assertThat(NivelPerigo.BAIXO.isPeloMenos(NivelPerigo.ALTO)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar comparação com nível nulo")
    void deveRejeitarComparacaoComNivelNulo() {
        assertThatThrownBy(() -> NivelPerigo.ALTO.isPeloMenos(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nível de perigo de comparação é obrigatório.");
    }
}
