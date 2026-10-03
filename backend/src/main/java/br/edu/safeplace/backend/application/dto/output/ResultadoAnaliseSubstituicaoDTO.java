package br.edu.safeplace.backend.application.dto.output;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resultado consolidado da Análise de Projeção de Desgaste (DS10 / UC10).
 * Contém projeções geradas com sucesso, pendências/falhas para nova tentativa e alertas preventivos.
 */
@Schema(description = "Resultado consolidado da Análise de Projeção de Desgaste de EPIs (UC10)")
public record ResultadoAnaliseSubstituicaoDTO(
        @Schema(description = "Lista de projeções calculadas e persistidas com sucesso")
        List<ProjecaoSubstituicaoOutputDTO> projecoes,

        @Schema(description = "Lista de pendências e falhas de cálculo que permitem nova tentativa")
        List<FalhaProjecaoOutputDTO> pendenciasFalhas,

        @Schema(description = "Lista de alertas preventivos emitidos")
        List<AlertaSubstituicaoOutputDTO> alertas,

        @Schema(description = "Total de EPIs processados na análise", example = "12")
        int totalProcessados,

        @Schema(description = "Total de projeções geradas com sucesso", example = "10")
        int totalSucesso,

        @Schema(description = "Total de pendências ou falhas registradas", example = "2")
        int totalPendencias
) {
    public static ResultadoAnaliseSubstituicaoDTO criar(
            List<ProjecaoSubstituicaoOutputDTO> projecoes,
            List<FalhaProjecaoOutputDTO> pendenciasFalhas,
            List<AlertaSubstituicaoOutputDTO> alertas) {
        int sucesso = projecoes != null ? projecoes.size() : 0;
        int falhas = pendenciasFalhas != null ? pendenciasFalhas.size() : 0;
        return new ResultadoAnaliseSubstituicaoDTO(
                projecoes != null ? projecoes : List.of(),
                pendenciasFalhas != null ? pendenciasFalhas : List.of(),
                alertas != null ? alertas : List.of(),
                sucesso + falhas,
                sucesso,
                falhas
        );
    }
}
