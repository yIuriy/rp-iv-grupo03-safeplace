package br.edu.safeplace.backend.domain.ocorrencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CausaRaizAcidenteTest {

    @Test
    @DisplayName("Deve conter todas as causas raiz requeridas pela regra de negócio")
    void deveConterTodasAsCausasRaizRequeridas() {
        assertThat(CausaRaizAcidente.values()).containsExactlyInAnyOrder(
                CausaRaizAcidente.FATOR_HUMANO,
                CausaRaizAcidente.FALHA_EPI,
                CausaRaizAcidente.OUTRO
        );
    }

    @Test
    @DisplayName("Lê causa válida sem diferenciar maiúsculas")
    void deveLerCausaValida() {
        assertThat(CausaRaizAcidente.de(" falha_epi ")).isEqualTo(CausaRaizAcidente.FALHA_EPI);
        assertThat(CausaRaizAcidente.de("FATOR_HUMANO")).isEqualTo(CausaRaizAcidente.FATOR_HUMANO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"QUEDA", "fator humano", "xyz"})
    @DisplayName("Recusa causa ausente ou inválida, sem converter para OUTRO (issue #195)")
    void deveRecusarCausaInvalida(String valor) {
        assertThatThrownBy(() -> CausaRaizAcidente.de(valor))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Causa raiz");
    }
}
