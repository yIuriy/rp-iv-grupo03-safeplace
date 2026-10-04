package br.edu.safeplace.backend.application.dto.input;

import java.util.List;

/**
 * Dados de entrada para vinculação de EPIs obrigatórios a uma tarefa (RF12 / UC08 / US12).
 */
public record AssociarEpisTarefaInputDTO(
        List<Integer> epiIds) {

    public AssociarEpisTarefaInputDTO {
        epiIds = epiIds == null ? List.of() : List.copyOf(epiIds);
    }
}
