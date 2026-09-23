package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificacaoTest {

    @Test
    @DisplayName("Deve calcular status EM_DIA quando validade for superior a 30 dias")
    void deveCalcularStatusEmDia() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        LocalDate vencimento = hoje.plusDays(31);
        Certificacao cert = new Certificacao(1, "NR-10 Básico", vencimento, hoje.minusMonths(6), 10);

        assertThat(cert.calcularStatus(hoje)).isEqualTo(StatusCertificacao.EM_DIA);
    }

    @Test
    @DisplayName("UC02: Deve calcular status PROXIMA_DO_VENCIMENTO exatamente no limiar de 30 dias")
    void deveCalcularStatusProximaDoVencimentoLimiar30Dias() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        LocalDate vencimento = hoje.plusDays(30);
        Certificacao cert = new Certificacao(1, "NR-35 Trabalho em Altura", vencimento, hoje.minusMonths(11), 10);

        assertThat(cert.calcularStatus(hoje)).isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
    }

    @Test
    @DisplayName("UC02: Deve calcular status PROXIMA_DO_VENCIMENTO com menos de 30 dias restantes")
    void deveCalcularStatusProximaDoVencimentoMenos30Dias() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        LocalDate vencimento = hoje.plusDays(15);
        Certificacao cert = new Certificacao(1, "NR-35", vencimento, hoje.minusMonths(11), 10);

        assertThat(cert.calcularStatus(hoje)).isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
    }

    @Test
    @DisplayName("UC02: Deve calcular status VENCIDA na data exata de vencimento ou após")
    void deveCalcularStatusVencida() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        Certificacao certVenceHoje = new Certificacao(1, "NR-10", hoje, hoje.minusYears(1), 10);
        Certificacao certVencidaOntem = new Certificacao(2, "NR-10", hoje.minusDays(1), hoje.minusYears(1), 10);

        assertThat(certVenceHoje.calcularStatus(hoje)).isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(certVencidaOntem.calcularStatus(hoje)).isEqualTo(StatusCertificacao.VENCIDA);
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com data de vencimento no passado ou presente")
    void deveRejeitarCadastroComVencimentoInvalido() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> Certificacao.cadastrar("NR-10", hoje.minusDays(1), hoje.minusMonths(1), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser no passado ou presente no momento do cadastro.");
    }

    @Test
    @DisplayName("Deve rejeitar emissão no futuro")
    void deveRejeitarEmissaoNoFuturo() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> new Certificacao(null, "NR-10", hoje.plusYears(1), hoje.plusDays(1), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de emissão não pode ser no futuro.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Deve rejeitar nome nulo ou em branco")
    void deveRejeitarNomeInvalido(String nomeInvalido) {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> new Certificacao(null, nomeInvalido, hoje.plusYears(1), hoje.minusMonths(1), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da certificação é obrigatório.");
    }

    @Test
    @DisplayName("Deve rejeitar certificação sem colaborador vinculado")
    void deveRejeitarSemColaborador() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> new Certificacao(null, "NR-10", hoje.plusYears(1), hoje.minusMonths(1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Colaborador é obrigatório.");
    }

    @Test
    @DisplayName("Diagrama de Classes: Deve validar inconsistência cronológica entre emissão e validade")
    void deveValidarInconsistenciaCronologica() {
        LocalDate hoje = LocalDate.now();
        LocalDate emissao = hoje.minusMonths(1);
        LocalDate vencimento = hoje.plusYears(1);

        assertThat(Certificacao.validarInconsistenciaCronologica(emissao, vencimento)).isFalse();
        assertThat(Certificacao.validarInconsistenciaCronologica(vencimento, emissao)).isTrue();
        assertThat(Certificacao.validarInconsistenciaCronologica(null, vencimento)).isFalse();
        assertThat(Certificacao.validarInconsistenciaCronologica(emissao, null)).isFalse();

        assertThatThrownBy(() -> new Certificacao(null, "NR-10", emissao, vencimento.minusYears(2).plusDays(100), 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
