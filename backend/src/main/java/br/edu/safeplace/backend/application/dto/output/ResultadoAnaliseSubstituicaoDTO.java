package br.edu.safeplace.backend.application.dto.output;

import java.util.List;

/**
 * Resultado consolidado da Análise de Projeção de Desgaste (DS10 / UC10).
 * Contém projeções geradas com sucesso, pendências/falhas para nova tentativa e alertas preventivos.
 */
public record ResultadoAnaliseSubstituicaoDTO(
        List<ProjecaoSubstituicaoOutputDTO> projecoes,
        List<FalhaProjecaoOutputDTO> pendenciasFalhas,
        List<AlertaSubstituicaoOutputDTO> alertas,
        int totalProcessados,
        int totalSucesso,
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
