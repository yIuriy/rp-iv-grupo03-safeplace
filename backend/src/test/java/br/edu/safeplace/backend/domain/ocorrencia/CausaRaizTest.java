package br.edu.safeplace.backend.domain.ocorrencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CausaRaizTest {

    @Test
    @DisplayName("Deve conter todas as causas raiz requeridas pela regra de negócio")
    void deveConterTodasAsCausasRaizRequeridas() {
        assertThat(CausaRaiz.values()).containsExactlyInAnyOrder(
                CausaRaiz.FATOR_HUMANO,
                CausaRaiz.FALHA_EPI,
                CausaRaiz.OUTRO
        );
    }
}
