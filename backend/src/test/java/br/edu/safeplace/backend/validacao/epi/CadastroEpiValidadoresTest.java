package br.edu.safeplace.backend.validacao.epi;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.epi.ModeloEPI;

/**
 * Número do CA e modelo, validados no cadastro antes de procurar o CA existente (issue #264). As
 * regras vieram de {@code ModeloEPI.deCadastroLegado}, do construtor de {@code ModeloEPI} e de
 * {@code EpiTest}/{@code ModeloEPITest}, com as mesmas mensagens.
 */
@DisplayName("NumeroCaValidador e ModeloEpiValidador - CA do cadastro de EPI (Chain of Responsibility)")
class CadastroEpiValidadoresTest {

    private final NumeroCaValidador numeroCa = new NumeroCaValidador();
    private final ModeloEpiValidador modelo = new ModeloEpiValidador();

    @ParameterizedTest
    @ValueSource(strings = {"1234", "CA-1234", " CA-1234 "})
    @DisplayName("Aceita CA com ou sem prefixo CA-")
    void aceitaNumeroValido(String numero) {
        assertThatCode(() -> numeroCa.validar(numero)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejeitaNumeroAusente(String numero) {
        assertThatThrownBy(() -> numeroCa.validar(numero))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número do CA é obrigatório.");
    }

    @Test
    void deveRejeitarCadastroComFormatoDeCaInvalido() {
        assertThatThrownBy(() -> numeroCa.validar("ABC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número do CA deve conter apenas dígitos, com prefixo CA- opcional.");
    }

    @Test
    void rejeitaNumeroAcimaDoLimiteInteiro() {
        assertThatThrownBy(() -> numeroCa.validar("99999999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número do CA excede o limite inteiro do modelo.");
    }

    @Test
    void deveRejeitarValidadeAusente() {
        assertThatThrownBy(() -> modelo.validar(new ModeloEPI(1234, "Marca Teste", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de validade do CA é obrigatória.");
    }

    @Test
    @DisplayName("CA zero é inválido; texto fora do formato vira CA zero na conversão")
    void rejeitaCaZero() {
        assertThatThrownBy(() -> modelo.validar(ModeloEPI.deCadastroLegado("CA-0", LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CA inválido.");
        assertThatThrownBy(() -> modelo.validar(ModeloEPI.deCadastroLegado("ABC", LocalDate.now())))
                .hasMessage("CA inválido.");
    }
}
