package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.AtualizarTarefaInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para atualização cadastral e relacional de tarefa (RF06, UC11)")
public record AtualizarTarefaRequest(
        @NotBlank(message = "Descrição é obrigatória")
        @Schema(description = "Descrição da atividade operacional", example = "Solda em altura na estrutura metálica")
        String descricao,

        @Schema(description = "Área de risco opcional vinculada à tarefa", example = "3")
        Integer areaRiscoId,

        @Schema(description = "Identificadores dos setores físicos vinculados à tarefa", example = "[1, 2]")
        List<Integer> setoresIds
) {
    public AtualizarTarefaInputDTO toInputDTO() {
        return new AtualizarTarefaInputDTO(descricao, areaRiscoId, setoresIds);
    }
}
