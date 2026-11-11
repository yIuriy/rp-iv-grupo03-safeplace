package br.edu.safeplace.backend.validacao.relatorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;

@DisplayName("PeriodoRelatorioValidador - período do relatório estatístico (RF18, #299)")
class PeriodoRelatorioValidadorTest {

    private final PeriodoRelatorioValidador validador = new PeriodoRelatorioValidador();

    @Test
    @DisplayName("Aceita um mês, um ano completo e um único dia")
    void aceitaPeriodosValidos() {
        assertThatCode(() -> validador.validar(new PeriodoRelatorio(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))))
                .doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(new PeriodoRelatorio(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))))
                .doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(new PeriodoRelatorio(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Sem uma das datas, interrompe a cadeia com uma única mensagem")
    void exigeAsDuasDatas() {
        PeriodoRelatorio semFim = new PeriodoRelatorio(LocalDate.of(2026, 11, 1), null);

        assertThat(validador.verificar(semFim).mensagens())
                .containsExactly("Informe as datas inicial e final do período do relatório.");
        assertThatThrownBy(() -> validador.validar(semFim)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Recusa período com início posterior ao fim")
    void recusaPeriodoInvertido() {
        assertThatThrownBy(() -> validador.validar(new PeriodoRelatorio(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data inicial do período não pode ser posterior à data final.");
    }

    @Test
    @DisplayName("Recusa período maior que um ano")
    void recusaPeriodoMaiorQueUmAno() {
        assertThatThrownBy(() -> validador.validar(new PeriodoRelatorio(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O período do relatório pode ter no máximo um ano.");
    }
}
