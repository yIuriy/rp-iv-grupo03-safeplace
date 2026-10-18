package br.edu.safeplace.backend.adapters.in.agendamento;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import br.edu.safeplace.backend.application.port.in.EmitirAlertasPrazoUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertaPrazoAcaoAgendadorTest {

    @Test
    @DisplayName("A rotina verifica os prazos na data de hoje")
    void verificaNaDataDeHoje() {
        EmitirAlertasPrazoUseCase useCase = mock(EmitirAlertasPrazoUseCase.class);
        when(useCase.emitirAlertasDePrazo(any(LocalDate.class))).thenReturn(2);
        LocalDate antes = LocalDate.now();

        new AlertaPrazoAcaoAgendador(useCase).verificarPrazos();

        verify(useCase).emitirAlertasDePrazo(org.mockito.ArgumentMatchers.argThat(
                data -> !data.isBefore(antes) && !data.isAfter(LocalDate.now())));
    }

    @Test
    @DisplayName("A rotina é agendada pelo cron configurável, de hora em hora por padrão")
    void agendadaPorCronConfiguravel() throws NoSuchMethodException {
        Scheduled agendamento = AlertaPrazoAcaoAgendador.class.getMethod("verificarPrazos").getAnnotation(Scheduled.class);

        assertThat(agendamento).isNotNull();
        assertThat(agendamento.cron()).isEqualTo("${safeplace.plano-acao.alertas.cron:0 0 * * * *}");
    }
}
