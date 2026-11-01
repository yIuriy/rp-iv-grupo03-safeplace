/*
 * Testes do Null Object OperadorAutenticadoNulo (issue #241): ele cumpre o contrato da porta
 * OperadorAutenticado como "ninguém autenticado", sem identificador e sem papel algum.
 */
package br.edu.safeplace.backend.adapters.out.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;

@DisplayName("OperadorAutenticadoNulo (Null Object)")
class OperadorAutenticadoNuloTest {

    private final OperadorAutenticado operador = new OperadorAutenticadoNulo();

    @Test
    @DisplayName("Não há identificador de operador: Optional vazio, nunca null")
    void semIdentificador() {
        assertThat(operador.obterIdentificadorOperador()).isNotNull().isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ROLE_GESTOR_SEGURANCA", "ROLE_SUPERVISOR", "GESTOR_SEGURANCA"})
    @DisplayName("Não possui papel nenhum")
    void semPapel(String papel) {
        assertThat(operador.possuiPapel(papel)).isFalse();
    }
}
