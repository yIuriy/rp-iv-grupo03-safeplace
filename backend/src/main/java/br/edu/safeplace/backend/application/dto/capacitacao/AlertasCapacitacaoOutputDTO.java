package br.edu.safeplace.backend.application.dto.capacitacao;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Painel de alertas do UC02: vencimentos próximos (regra 1), certificações vencidas (cenário
 * alternativo I) e pendências de integração (cenário alternativo III).
 */
@Schema(description = "Alertas de capacitação calculados na data de referência (UC02, US10)")
public record AlertasCapacitacaoOutputDTO(
        @Schema(description = "Data usada no cálculo das situações", example = "2026-10-04") LocalDate dataReferencia,
        @Schema(description = "Antecedência, em dias, do alerta de vencimento", example = "30") int diasAntecedenciaAlerta,
        @Schema(description = "Certificações que vencem dentro da antecedência de alerta, da mais próxima para a mais distante")
        List<AlertaCertificacaoOutputDTO> proximasDoVencimento,
        @Schema(description = "Certificações vencidas, da mais antiga para a mais recente")
        List<AlertaCertificacaoOutputDTO> vencidas,
        @Schema(description = "Colaboradores sem histórico de capacitação")
        List<PendenciaColaboradorOutputDTO> pendencias
) {
}
