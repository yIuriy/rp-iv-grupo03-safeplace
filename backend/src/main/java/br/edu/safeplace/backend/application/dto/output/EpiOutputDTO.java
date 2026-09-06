package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

import java.time.LocalDate;

public record EpiOutputDTO(
        Integer id,
        String nome,
        String numeroCa,
        int quantidade,
        int estoqueMinimo,
        StatusEpi status,
        LocalDate dataValidadeCa,
        Integer vidaUtilDias,
        boolean estoqueCritico
) {
    public static EpiOutputDTO deDominio(Epi epi) {
        return new EpiOutputDTO(
                epi.getId(),
                epi.getNome(),
                epi.getNumeroCa(),
                epi.getQuantidade(),
                epi.getEstoqueMinimo(),
                epi.getStatus(),
                epi.getDataValidadeCa(),
                epi.getVidaUtilDias(),
                epi.isEstoqueCritico()
        );
    }
}
