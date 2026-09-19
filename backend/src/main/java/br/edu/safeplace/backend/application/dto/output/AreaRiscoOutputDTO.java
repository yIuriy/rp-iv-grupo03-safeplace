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
        Integer setorId,
        List<String> agentesRisco,
        String limites,
        List<EpiResumoOutputDTO> episObrigatorios) {

    public AreaRiscoOutputDTO(
            Integer id,
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            List<EpiResumoOutputDTO> episObrigatorios) {
        this(id, codigo, nome, descricao, nivelPerigo, null, java.util.Collections.emptyList(), null, episObrigatorios);
    }

    public static AreaRiscoOutputDTO deDominio(AreaRisco areaRisco) {
        if (areaRisco == null) {
            return null;
        }
        return new AreaRiscoOutputDTO(
                areaRisco.getId(),
                areaRisco.getCodigo(),
                areaRisco.getNome(),
                areaRisco.getDescricao(),
                areaRisco.getNivelPerigo(),
                areaRisco.getSetorId(),
                areaRisco.getAgentesRisco(),
                areaRisco.getLimites(),
                areaRisco.verificarEpisObrigatoriosArea().stream()
                        .map(EpiResumoOutputDTO::deDominio)
                        .toList());
    }
}
