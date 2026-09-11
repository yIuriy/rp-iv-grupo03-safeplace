package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificadoAprovacaoTest {

    private static final LocalDate REFERENCIA =
            LocalDate.of(2026, 9, 11);

    @ParameterizedTest
    @ValueSource(strings = {"1234", "CA-1234", "  CA-1234  "})
    void deveCriarCertificadoComNumeroNormalizado(String numero) {
        LocalDate validade = REFERENCIA.plusDays(1);

        CertificadoAprovacao ca =
                new CertificadoAprovacao(numero, validade);

        assertThat(ca.numero()).isEqualTo("1234");
        assertThat(ca.dataValidade()).isEqualTo(validade);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void deveRejeitarNumeroAusente(String numero) {
        assertThatThrownBy(() ->
                new CertificadoAprovacao(numero, REFERENCIA)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número do CA é obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CA-",
            "ABC",
            "12A34",
            "-1234",
            "12.34",
            "12 34"
    })
    void deveRejeitarFormatoInvalido(String numero) {
        assertThatThrownBy(() ->
                new CertificadoAprovacao(numero, REFERENCIA)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Número do CA deve conter apenas dígitos, com prefixo CA- opcional."
                );
    }

    @Test
    void deveRejeitarValidadeAusente() {
        assertThatThrownBy(() ->
                new CertificadoAprovacao("1234", null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de validade do CA é obrigatória.");
    }

    @Test
    void devePermitirRepresentarCertificadoVencidoParaHistorico() {
        CertificadoAprovacao ca = new CertificadoAprovacao(
                "1234",
                REFERENCIA.minusDays(1)
        );

        assertThat(ca.estaVencidoEm(REFERENCIA)).isTrue();
    }

    @Test
    void deveRejeitarCertificadoVencidoNaValidacaoDeCadastro() {
        CertificadoAprovacao ca = new CertificadoAprovacao(
                "1234",
                REFERENCIA.minusDays(1)
        );

        assertThatThrownBy(() ->
                ca.validarParaCadastroEm(REFERENCIA)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Não é permitido cadastrar EPI com CA vencido."
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 365})
    void deveAceitarCadastroAteODiaDaValidade(int diasAteVencimento) {
        CertificadoAprovacao ca = new CertificadoAprovacao(
                "1234",
                REFERENCIA.plusDays(diasAteVencimento)
        );

        assertThat(ca.estaVencidoEm(REFERENCIA)).isFalse();

        assertThatCode(() ->
                ca.validarParaCadastroEm(REFERENCIA)
        ).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarConsultaSemDataDeReferencia() {
        CertificadoAprovacao ca =
                new CertificadoAprovacao("1234", REFERENCIA);

        assertThatThrownBy(() -> ca.estaVencidoEm(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de referência é obrigatória.");
    }

    @Test
    void deveRejeitarCadastroSemDataDeReferencia() {
        CertificadoAprovacao ca =
                new CertificadoAprovacao("1234", REFERENCIA);

        assertThatThrownBy(() -> ca.validarParaCadastroEm(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de referência é obrigatória.");
    }

    @Test
    void deveCompararCertificadosPelosValores() {
        CertificadoAprovacao primeiro =
                new CertificadoAprovacao("CA-1234", REFERENCIA);

        CertificadoAprovacao equivalente =
                new CertificadoAprovacao("1234", REFERENCIA);

        CertificadoAprovacao numeroDiferente =
                new CertificadoAprovacao("5678", REFERENCIA);

        CertificadoAprovacao validadeDiferente =
                new CertificadoAprovacao("1234", REFERENCIA.plusDays(1));

        assertThat(primeiro).isEqualTo(equivalente);
        assertThat(primeiro.hashCode()).isEqualTo(equivalente.hashCode());
        assertThat(primeiro).isNotEqualTo(numeroDiferente);
        assertThat(primeiro).isNotEqualTo(validadeDiferente);
    }
}