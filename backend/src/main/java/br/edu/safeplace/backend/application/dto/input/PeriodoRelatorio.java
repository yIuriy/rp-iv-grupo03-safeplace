/*
 * Período de apuração de um relatório estatístico (RF18, issue #299): os dois dias informados fazem
 * parte do período. As regras (datas obrigatórias, ordem e duração máxima) ficam no
 * PeriodoRelatorioValidador, na camada de validação.
 */
package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PeriodoRelatorio(LocalDate inicio, LocalDate fim) {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Verdadeiro quando a data está entre o início e o fim, inclusive. */
    public boolean contem(LocalDate data) {
        return data != null && !data.isBefore(inicio) && !data.isAfter(fim);
    }

    public boolean contem(LocalDateTime instante) {
        return instante != null && contem(instante.toLocalDate());
    }

    /** Mesmo período como critério da consulta de ocorrências, que usa a data do fato. */
    public FiltroOcorrenciasDTO comoFiltroDeOcorrencias() {
        return new FiltroOcorrenciasDTO(inicio, fim, null, null);
    }

    /** Ex.: "01/11/2026 a 30/11/2026". */
    public String descrever() {
        return DATA.format(inicio) + " a " + DATA.format(fim);
    }
}
