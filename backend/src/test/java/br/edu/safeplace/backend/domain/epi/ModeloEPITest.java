package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModeloEPITest {

    private static final LocalDate REFERENCIA =
            LocalDate.of(2026, 9, 11);

    @Test
    void devePreservarDadosDoModelo() {
        LocalDate validade = REFERENCIA.plusYears(1);

        ModeloEPI modelo = new ModeloEPI(
                1234,
                "Marca Teste",
                validade
        );

        assertThat(modelo.getCa()).isEqualTo(1234);
        assertThat(modelo.getMarca()).isEqualTo("Marca Teste");
        assertThat(modelo.getValidadeCA()).isEqualTo(validade);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, false",
            "0, true",
            "1, true"
    })
    void deveVerificarValidadeNaDataInformada(
            int diasAteVencimento,
            boolean esperado
    ) {
        ModeloEPI modelo = new ModeloEPI(
                1234,
                "Marca Teste",
                REFERENCIA.plusDays(diasAteVencimento)
        );

        assertThat(modelo.verificarCA(REFERENCIA))
                .isEqualTo(esperado);
    }

    @Test
    void devePermitirRepresentarModeloComCaVencido() {
        LocalDate validadeAntiga = LocalDate.of(2000, 1, 1);

        ModeloEPI modelo = new ModeloEPI(
                1234,
                "Marca Teste",
                validadeAntiga
        );

        assertThat(modelo.getValidadeCA()).isEqualTo(validadeAntiga);
        assertThat(modelo.verificarCA(REFERENCIA)).isFalse();
    }

    @Test
    void deveRejeitarValidadeAusente() {
        assertThatThrownBy(() ->
                new ModeloEPI(1234, "Marca Teste", null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de validade do CA é obrigatória.");
    }

    @Test
    void deveRejeitarReferenciaAusente() {
        ModeloEPI modelo = new ModeloEPI(
                1234,
                "Marca Teste",
                REFERENCIA
        );

        assertThatThrownBy(() -> modelo.verificarCA(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de referência é obrigatória.");
    }

    @Test
    void deveVerificarCaUsandoDataAtual() {
        ModeloEPI vencido = new ModeloEPI(
                1234, "Marca Teste", LocalDate.MIN
        );

        ModeloEPI vigente = new ModeloEPI(
                5678, "Marca Teste", LocalDate.MAX
        );

        assertThat(vencido.verificarCA()).isFalse();
        assertThat(vigente.verificarCA()).isTrue();
    }

    @Test
    void deveGuardarMarcaDoCadastroLegadoSemEspacos() {
        ModeloEPI modelo = ModeloEPI.deCadastroLegado("CA-1234", "  Protege ", REFERENCIA);

        assertThat(modelo.getCa()).isEqualTo(1234);
        assertThat(modelo.getMarca()).isEqualTo("Protege");
        assertThat(ModeloEPI.deCadastroLegado("1234", " ", REFERENCIA).getMarca()).isNull();
    }

    @Test
    void deveReutilizarModeloDoMesmoCaECompletarMarcaAusente() {
        ModeloEPI existente = new ModeloEPI(9, 1234, null, REFERENCIA);

        ModeloEPI reutilizado = existente.reutilizarPara(new ModeloEPI(1234, "Protege", REFERENCIA));

        assertThat(reutilizado.getId()).isEqualTo(9);
        assertThat(reutilizado.getMarca()).isEqualTo("Protege");
        assertThat(reutilizado.getValidadeCA()).isEqualTo(REFERENCIA);
    }

    @Test
    void deveManterMarcaExistenteQuandoCadastroNaoInformaMarca() {
        ModeloEPI existente = new ModeloEPI(9, 1234, "Protege", REFERENCIA);

        assertThat(existente.reutilizarPara(new ModeloEPI(1234, null, REFERENCIA)).getMarca())
                .isEqualTo("Protege");
    }

    @Test
    void deveRecusarMesmoCaComValidadeOuMarcaDivergente() {
        ModeloEPI existente = new ModeloEPI(9, 1234, "Protege", REFERENCIA);

        assertThatThrownBy(() -> existente.reutilizarPara(new ModeloEPI(1234, "Protege", REFERENCIA.plusDays(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já cadastrado com validade");
        assertThatThrownBy(() -> existente.reutilizarPara(new ModeloEPI(1234, "Outra", REFERENCIA)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já cadastrado com a marca");
        assertThatThrownBy(() -> existente.reutilizarPara(new ModeloEPI(5678, "Protege", REFERENCIA)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}