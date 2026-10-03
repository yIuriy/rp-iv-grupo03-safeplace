package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída com os dados de uma projeção de substituição calculada (UC10, RF21).
 */
@Schema(description = "Projeção de substituição calculada para um EPI")
public record ProjecaoSubstituicaoOutputDTO(
        @Schema(description = "Identificador da projeção persistida", example = "1")
        Integer id,

        @Schema(description = "Identificador do EPI", example = "1")
        Integer epiId,

        @Schema(description = "Nome do EPI", example = "Capacete de Segurança")
        String nomeEpi,

        @Schema(description = "Data em que a projeção foi calculada")
        LocalDate dataProjecao,

        @Schema(description = "Data estimada para o descarte ou substituição")
        LocalDate dataEstimadaDescarte,

        @Schema(description = "Justificativa e parâmetros considerados no cálculo")
        String justificativa,

        @Schema(description = "Data de validade do CA do EPI")
        LocalDate validadeCa,

        @Schema(description = "Quantidade atual disponível no estoque", example = "10")
        int quantidadeDisponivel,

        @Schema(description = "Estoque mínimo configurado", example = "2")
        int estoqueMinimo
) {
    public static ProjecaoSubstituicaoOutputDTO aPartirDe(ProjecaoSubstituicao projecao, String nomeEpi,
                                                          LocalDate validadeCa, int quantidade, int estoqueMinimo) {
        return new ProjecaoSubstituicaoOutputDTO(
                projecao.getId(),
                projecao.getEpiId(),
                nomeEpi,
                projecao.getDataProjecao(),
                projecao.getDataEstimadaDescarte(),
                projecao.getJustificativa(),
                validadeCa,
                quantidade,
                estoqueMinimo
        );
    }
}
