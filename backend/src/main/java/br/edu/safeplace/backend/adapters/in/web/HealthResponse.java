package br.edu.safeplace.backend.adapters.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de status do sistema")
public record HealthResponse(
        @Schema(description = "Estado do serviço", example = "UP")
        String status,

        @Schema(description = "Identificador do serviço", example = "safeplace-backend")
        String service
) {
}
