package br.edu.safeplace.backend.application.dto.input;

import java.util.List;

public record AtualizarTarefaInputDTO(
        String descricao,
        Integer areaRiscoId,
        List<Integer> setoresIds
) {
}
