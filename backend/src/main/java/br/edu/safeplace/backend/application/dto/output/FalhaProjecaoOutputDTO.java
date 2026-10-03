package br.edu.safeplace.backend.application.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para reportar falhas ou inconsistências em EPIs que impediram o cálculo da projeção
 * (UC10 Exceção I). Permite ao gestor retificar os dados e solicitar nova tentativa.
 */
@Schema(description = "Reporte de pendência ou falha de cálculo para um EPI, permitindo nova tentativa")
public record FalhaProjecaoOutputDTO(
        @Schema(description = "Identificador do EPI com pendência", example = "2")
        Integer epiId,

        @Schema(description = "Nome do EPI", example = "Luva de Proteção Mecânica")
        String nomeEpi,

        @Schema(description = "Motivo do impedimento do cálculo da projeção", example = "EPI não possui setor com periculosidade associado")
        String motivoFalha
) {
}
