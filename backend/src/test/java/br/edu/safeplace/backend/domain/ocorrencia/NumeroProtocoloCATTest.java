package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumeroProtocoloCATTest {

    @Test
    @DisplayName("Deve instanciar NumeroProtocoloCAT com valor válido no formato CAT-ANO-MES-SEQUENCIAL")
    void deveInstanciarProtocoloCATValido() {
        NumeroProtocoloCAT cat = NumeroProtocoloCAT.de("CAT-2026-09-0001");
        assertThat(cat.getValor()).isEqualTo("CAT-2026-09-0001");
        assertThat(cat.valor()).isEqualTo("CAT-2026-09-0001");
        assertThat(cat.toString()).isEqualTo("CAT-2026-09-0001");
    }

    @Test
    @DisplayName("Deve gerar NumeroProtocoloCAT a partir de data de referência e número sequencial")
    void deveGerarProtocoloCATComDataESequencial() {
        LocalDateTime data = LocalDateTime.of(2026, 9, 15, 10, 30);
        NumeroProtocoloCAT cat = NumeroProtocoloCAT.gerar(data, 1);
        assertThat(cat.getValor()).isEqualTo("CAT-2026-09-0001");
    }

    @Test
    @DisplayName("Deve gerar NumeroProtocoloCAT usando data atual e número sequencial")
    void deveGerarProtocoloCATComDataAtualESequencial() {
        LocalDateTime agora = LocalDateTime.now();
        NumeroProtocoloCAT cat = NumeroProtocoloCAT.gerar(42);
        String prefixoEsperado = String.format("CAT-%d-%02d-0042", agora.getYear(), agora.getMonthValue());
        assertThat(cat.getValor()).isEqualTo(prefixoEsperado);
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "	"})
    @DisplayName("Valor nulo ou em branco não é um protocolo válido")
    void deveRejeitarValorNuloOuEmBranco(String valorInvalido) {
        assertThat(NumeroProtocoloCAT.de(valorInvalido).formatoValido()).isFalse();
    }
    @ParameterizedTest
    @ValueSource(strings = {
            "CAT-2026-9-0001",
            "CAT-26-09-0001",
            "CAT-2026-09-01",
            "2026-09-0001",
            "CAT_2026_09_0001",
            "PROTOCOLO-123"
    })
    @DisplayName("Formatos inválidos não seguem o padrão; a recusa é do OcorrenciaValidador (issue #266)")
    void deveRejeitarFormatosInvalidos(String valorInvalido) {
        assertThat(NumeroProtocoloCAT.de(valorInvalido).formatoValido()).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar sequencial menor ou igual a zero ao gerar")
    void deveRejeitarSequencialInvalido() {
        assertThatThrownBy(() -> NumeroProtocoloCAT.gerar(0))
                .isInstanceOf(ProtocoloCATInvalidoException.class)
                .hasMessage("Número sequencial deve ser positivo.");

        assertThatThrownBy(() -> NumeroProtocoloCAT.gerar(-5))
                .isInstanceOf(ProtocoloCATInvalidoException.class)
                .hasMessage("Número sequencial deve ser positivo.");
    }

    @Test
    @DisplayName("Deve respeitar igualdade e hashCode por valor")
    void deveRespeitarEqualsEHashCode() {
        NumeroProtocoloCAT cat1 = NumeroProtocoloCAT.de("CAT-2026-09-0001");
        NumeroProtocoloCAT cat2 = NumeroProtocoloCAT.de("cat-2026-09-0001");

        assertThat(cat1).isEqualTo(cat2);
        assertThat(cat1.hashCode()).isEqualTo(cat2.hashCode());
    }
}
