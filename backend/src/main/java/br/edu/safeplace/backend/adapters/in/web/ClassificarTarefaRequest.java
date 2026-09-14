package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para classificar ou reavaliar o nível de periculosidade de uma tarefa (UC11)")
public record ClassificarTarefaRequest(
        @NotNull(message = "Nível de perigo é obrigatório") @Schema(description = "Grau de periculosidade atribuído à atividade", example = "CRITICO") NivelPerigo nivelPerigo) {
}
