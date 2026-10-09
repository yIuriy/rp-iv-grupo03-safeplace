package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Regra de antecedência de alerta compartilhada por certificações e treinamentos (UC02, regra 1). */
public final class PrazoVencimento {
    public static final int DIAS_ANTECEDENCIA_ALERTA = 30;

    private PrazoVencimento() {
    }

    /** Na própria data de vencimento o item já está vencido (UC02, cenário alternativo I). */
    public static boolean estaVencido(LocalDate dataReferencia, LocalDate dataVencimento) {
        return !dataReferencia.isBefore(dataVencimento);
    }

    public static boolean estaProximoDoVencimento(LocalDate dataReferencia, LocalDate dataVencimento) {
        return !estaVencido(dataReferencia, dataVencimento)
                && ChronoUnit.DAYS.between(dataReferencia, dataVencimento) <= DIAS_ANTECEDENCIA_ALERTA;
    }
}
