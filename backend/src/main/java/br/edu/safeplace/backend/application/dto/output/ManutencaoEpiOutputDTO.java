package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;

import java.time.LocalDateTime;

public record ManutencaoEpiOutputDTO(
        Integer id,
        Integer epiId,
        LocalDateTime dataManutencao,
        TipoManutencao tipoManutencao,
        String descricao,
        ResultadoManutencao resultado,
        String responsavelManutencao,
        StatusEpi statusAtualEpi
) {
    public static ManutencaoEpiOutputDTO deDominio(ManutencaoEpi manutencao, StatusEpi statusAtualEpi) {
        return new ManutencaoEpiOutputDTO(
                manutencao.getId(),
                manutencao.getEpiId(),
                manutencao.getDataManutencao(),
                manutencao.getTipoManutencao(),
                manutencao.getDescricao(),
                manutencao.getResultado(),
                manutencao.getResponsavelManutencao(),
                statusAtualEpi
        );
    }
}
