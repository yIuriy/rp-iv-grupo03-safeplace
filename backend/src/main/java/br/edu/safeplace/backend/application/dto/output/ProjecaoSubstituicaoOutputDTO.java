package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;

/**
 * DTO de saída com os dados de uma projeção de substituição calculada (UC10, RF21).
 */
public record ProjecaoSubstituicaoOutputDTO(
        Integer id,
        Integer epiId,
        String nomeEpi,
        LocalDate dataProjecao,
        LocalDate dataEstimadaDescarte,
        String justificativa,
        LocalDate validadeCa,
        int quantidadeDisponivel,
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
