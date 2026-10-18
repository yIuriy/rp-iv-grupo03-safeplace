package br.edu.safeplace.backend.domain.ocorrencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoOcorrenciaTest {

    @Test
    @DisplayName("Lê o tipo sem diferenciar maiúsculas")
    void deveLerTipoValido() {
        assertThat(TipoOcorrencia.de("acidente")).isEqualTo(TipoOcorrencia.ACIDENTE);
        assertThat(TipoOcorrencia.de(" INCIDENTE ")).isEqualTo(TipoOcorrencia.INCIDENTE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"QUASE_ACIDENTE", "xyz"})
    @DisplayName("Recusa tipo ausente ou desconhecido")
    void deveRecusarTipoInvalido(String valor) {
        assertThatThrownBy(() -> TipoOcorrencia.de(valor))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Tipo de ocorrência");
    }
}
