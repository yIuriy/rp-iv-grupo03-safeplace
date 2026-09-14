package br.edu.safeplace.backend.application.dto.output;

import java.util.List;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record AreaRiscoOutputDTO(
        Integer id,
        String codigo,
        String nome,
        String descricao,
        NivelPerigo nivelPerigo,
        List<EpiResumoOutputDTO> episObrigatorios) {

    public static AreaRiscoOutputDTO deDominio(AreaRisco areaRisco) {
        return new AreaRiscoOutputDTO(
                areaRisco.getId(),
                areaRisco.getCodigo(),
                areaRisco.getNome(),
                areaRisco.getDescricao(),
                areaRisco.getNivelPerigo(),
                areaRisco.verificarEpisObrigatoriosArea().stream()
                        .map(EpiResumoOutputDTO::deDominio)
                        .toList());
    }
}
