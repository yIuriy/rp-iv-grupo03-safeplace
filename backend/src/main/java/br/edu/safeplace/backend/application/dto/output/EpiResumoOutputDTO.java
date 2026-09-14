package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.epi.Epi;

/**
 * Identificação enxuta de um EPI, usada quando ele aparece como item vinculado a outro agregado
 * (por exemplo, os EPIs obrigatórios de acesso a uma área de risco).
 */
public record EpiResumoOutputDTO(
        Integer id,
        String nome,
        String numeroCa) {

    public static EpiResumoOutputDTO deDominio(Epi epi) {
        return new EpiResumoOutputDTO(epi.getId(), epi.getNome(), epi.getNumeroCa());
    }
}
