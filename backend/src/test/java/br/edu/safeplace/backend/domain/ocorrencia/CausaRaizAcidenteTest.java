package br.edu.safeplace.backend.domain.ocorrencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
