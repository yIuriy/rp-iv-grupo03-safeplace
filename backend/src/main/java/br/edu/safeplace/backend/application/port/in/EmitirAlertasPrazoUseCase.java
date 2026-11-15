package br.edu.safeplace.backend.application.port.in;

import java.time.LocalDate;

/** Verificação periódica dos alertas de prazo das ações abertas (UC07, regra 2; issue #196). */
public interface EmitirAlertasPrazoUseCase {

    /**
     * Emite o alerta de cada ação aberta cuja data de alerta já chegou e que ainda não foi avisada
     * para o prazo vigente. Devolve quantos alertas novos foram emitidos.
     */
    int emitir(LocalDate dataReferencia);
}
