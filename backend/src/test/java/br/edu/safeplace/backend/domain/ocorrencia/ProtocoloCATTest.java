package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProtocoloCATTest {

    @Test
    @DisplayName("Deve instanciar ProtocoloCAT com valor válido no formato CAT-ANO-MES-SEQUENCIAL")
    void deveInstanciarProtocoloCATValido() {
        ProtocoloCAT cat = ProtocoloCAT.de("CAT-2026-09-0001");

        assertThat(cat.getValor()).isEqualTo("CAT-2026-09-0001");
        assertThat(cat.toString()).isEqualTo("CAT-2026-09-0001");
    }

    @Test
    @DisplayName("Deve gerar ProtocoloCAT a partir de data de referência e número sequencial")
    void deveGerarProtocoloCATComDataESequencial() {
        LocalDateTime data = LocalDateTime.of(2026, 9, 13, 10, 0);
        ProtocoloCAT cat = ProtocoloCAT.gerar(data, 1);

        assertThat(cat.getValor()).isEqualTo("CAT-2026-09-0001");
    }

    @Test
    @DisplayName("Deve gerar ProtocoloCAT usando data atual e número sequencial")
    void deveGerarProtocoloCATComDataAtualESequencial() {
        LocalDateTime agora = LocalDateTime.now();
        ProtocoloCAT cat = ProtocoloCAT.gerar(42);

        String esperadoInicio = String.format("CAT-%d-%02d-0042", agora.getYear(), agora.getMonthValue());
        assertThat(cat.getValor()).isEqualTo(esperadoInicio);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "12345", "INVALIDO", "OC-2026-09-0001", "CAT-2026-9-0001", "CAT-26-09-0001"})
    @DisplayName("Deve rejeitar formatos inválidos de ProtocoloCAT")
    void deveRejeitarFormatosInvalidos(String valorInvalido) {
        assertThatThrownBy(() -> ProtocoloCAT.de(valorInvalido))
                .isInstanceOf(ProtocoloCATInvalidoException.class);
    }

    @Test
    @DisplayName("Deve rejeitar número sequencial menor ou igual a zero ao gerar")
    void deveRejeitarSequencialInvalido() {
        assertThatThrownBy(() -> ProtocoloCAT.gerar(LocalDateTime.now(), 0))
                .isInstanceOf(ProtocoloCATInvalidoException.class)
                .hasMessage("Número sequencial deve ser positivo.");

        assertThatThrownBy(() -> ProtocoloCAT.gerar(LocalDateTime.now(), -5))
                .isInstanceOf(ProtocoloCATInvalidoException.class);
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode por valor")
    void deveImplementarEqualsEHashCode() {
        ProtocoloCAT cat1 = ProtocoloCAT.de("CAT-2026-09-0001");
        ProtocoloCAT cat2 = ProtocoloCAT.de("CAT-2026-09-0001");
        ProtocoloCAT cat3 = ProtocoloCAT.de("CAT-2026-09-0002");

        assertThat(cat1).isEqualTo(cat2);
        assertThat(cat1.hashCode()).isEqualTo(cat2.hashCode());
        assertThat(cat1).isNotEqualTo(cat3);
    }
}
