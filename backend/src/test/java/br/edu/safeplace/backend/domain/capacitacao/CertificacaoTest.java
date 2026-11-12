package br.edu.safeplace.backend.domain.capacitacao;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CertificacaoTest {

    @Test
    @DisplayName("Deve calcular status EM_DIA quando validade for superior a 30 dias")
    void deveCalcularStatusEmDia() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        LocalDate vencimento = hoje.plusDays(31);
        Certificacao cert = new Certificacao(1, "NR-10 Básico", vencimento, hoje.minusMonths(6), pessoa(10));

        assertThat(cert.calcularStatus(hoje)).isEqualTo(StatusCertificacao.EM_DIA);
    }

    @Test
    @DisplayName("UC02: Deve calcular status PROXIMA_DO_VENCIMENTO exatamente no limiar de 30 dias")
    void deveCalcularStatusProximaDoVencimentoLimiar30Dias() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        LocalDate vencimento = hoje.plusDays(30);
        Certificacao cert = new Certificacao(1, "NR-35 Trabalho em Altura", vencimento, hoje.minusMonths(11), pessoa(10));

        assertThat(cert.calcularStatus(hoje)).isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
    }

    @Test
    @DisplayName("UC02: Deve calcular status PROXIMA_DO_VENCIMENTO com menos de 30 dias restantes")
    void deveCalcularStatusProximaDoVencimentoMenos30Dias() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        LocalDate vencimento = hoje.plusDays(15);
        Certificacao cert = new Certificacao(1, "NR-35", vencimento, hoje.minusMonths(11), pessoa(10));

        assertThat(cert.calcularStatus(hoje)).isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
    }

    @Test
    @DisplayName("UC02: Deve calcular status VENCIDA na data exata de vencimento ou após")
    void deveCalcularStatusVencida() {
        LocalDate hoje = LocalDate.of(2026, 9, 21);
        Certificacao certVenceHoje = new Certificacao(1, "NR-10", hoje, hoje.minusYears(1), pessoa(10));
        Certificacao certVencidaOntem = new Certificacao(2, "NR-10", hoje.minusDays(1), hoje.minusYears(1), pessoa(10));

        assertThat(certVenceHoje.calcularStatus(hoje)).isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(certVencidaOntem.calcularStatus(hoje)).isEqualTo(StatusCertificacao.VENCIDA);
    }

    @Test
    @DisplayName("UC02: Certificação habilita até a véspera do vencimento e deixa de habilitar na data de vencimento")
    void deveSerValidaAteAVesperaDoVencimento() {
        LocalDate vencimento = LocalDate.of(2026, 9, 21);
        Certificacao cert = new Certificacao(1, "NR-10", vencimento, vencimento.minusYears(1), pessoa(10));

        assertThat(cert.estaValidaEm(vencimento.minusDays(1))).isTrue();
        assertThat(cert.estaValidaEm(vencimento)).isFalse();
        assertThat(cert.estaValidaEm(vencimento.plusDays(1))).isFalse();
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
    }
}
