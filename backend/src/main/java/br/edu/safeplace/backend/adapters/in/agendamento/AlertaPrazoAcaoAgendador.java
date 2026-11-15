/*
 * Rotina periódica que emite os alertas de prazo das ações do plano (UC07, regra 2; US07, critério 5;
 * issue #196). Roda de hora em hora por padrão (safeplace.plano-acao.alertas.cron): como cada alerta é
 * emitido uma única vez por ação e prazo, repetir a verificação não duplica avisos, e um servidor que
 * estava desligado no horário recupera os alertas na execução seguinte.
 */
package br.edu.safeplace.backend.adapters.in.agendamento;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.in.EmitirAlertasPrazoUseCase;

@Component
public class AlertaPrazoAcaoAgendador {

    private static final Logger log = LoggerFactory.getLogger(AlertaPrazoAcaoAgendador.class);

    private final EmitirAlertasPrazoUseCase emitirAlertasPrazo;

    public AlertaPrazoAcaoAgendador(EmitirAlertasPrazoUseCase emitirAlertasPrazo) {
        this.emitirAlertasPrazo = emitirAlertasPrazo;
    }

    @Scheduled(cron = "${safeplace.plano-acao.alertas.cron:0 0 * * * *}")
    public void verificarPrazos() {
        int emitidos = emitirAlertasPrazo.emitir(LocalDate.now());
        if (emitidos > 0) {
            log.info("Alertas de prazo de ações emitidos: {}", emitidos);
        }
    }
}
