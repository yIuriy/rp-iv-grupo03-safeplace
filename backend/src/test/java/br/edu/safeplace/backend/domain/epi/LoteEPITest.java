package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoteEPITest {

    @Test
    void devePreservarDadosDoLoteEModeloAssociado() {
        LocalDate fabricacao = LocalDate.of(2026, 1, 10);
        LocalDate validadeLote = LocalDate.of(2028, 1, 10);
        LocalDate validadeCa = LocalDate.of(2027, 12, 31);

        ModeloEPI modelo = new ModeloEPI(
                1234,
                "Marca Teste",
                validadeCa
        );

        LoteEPI lote = new LoteEPI(
                "LOTE-001",
                "NF-123",
                fabricacao,
                validadeLote,
                20,
                modelo
        );

        assertThat(lote.getNumeroLote()).isEqualTo("LOTE-001");
        assertThat(lote.getNotaFiscal()).isEqualTo("NF-123");
        assertThat(lote.getDataFabricacao()).isEqualTo(fabricacao);
        assertThat(lote.getValidade()).isEqualTo(validadeLote);
        assertThat(lote.getQuantidadeRecebida()).isEqualTo(20);
        assertThat(lote.getModelo()).isSameAs(modelo);

        assertThat(lote.getModelo().getValidadeCA())
                .isEqualTo(validadeCa);
    }

    @Test
    void devePermitirVariosLotesDoMesmoModelo() {
        ModeloEPI modelo = new ModeloEPI(
                1234,
                "Marca Teste",
                LocalDate.of(2028, 12, 31)
        );

        LoteEPI primeiro = new LoteEPI(
                "LOTE-001",
                "NF-100",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                10,
                modelo
        );

        LoteEPI segundo = new LoteEPI(
                "LOTE-002",
                "NF-200",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2027, 2, 1),
                30,
                modelo
        );

        assertThat(primeiro.getModelo())
                .isSameAs(segundo.getModelo());

        assertThat(primeiro.getQuantidadeRecebida()).isEqualTo(10);
        assertThat(segundo.getQuantidadeRecebida()).isEqualTo(30);
    }

    @Test
    void deveRejeitarLoteSemModelo() {
        assertThatThrownBy(() -> new LoteEPI(
                "LOTE-001",
                "NF-123",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                20,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Modelo do EPI é obrigatório para o lote.");
    }
}