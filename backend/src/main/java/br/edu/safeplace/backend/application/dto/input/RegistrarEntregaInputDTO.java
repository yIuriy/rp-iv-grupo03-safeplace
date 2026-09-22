/*
 * Dados de entrada para registrar a entrega de um EPI a um colaborador (UC12, passos 3 a 6).
 * confirmarSubstituicao e justificativaSubstituicao atendem à exceção III do UC12 (duplicidade de
 * tipo em posse ativa). O operador não vem aqui: é o usuário autenticado.
 */
package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

public record RegistrarEntregaInputDTO(
        Integer epiId,
        Integer colaboradorId,
        LocalDate dataDevolucaoPrevista,
        boolean confirmarSubstituicao,
        String justificativaSubstituicao) {
}
