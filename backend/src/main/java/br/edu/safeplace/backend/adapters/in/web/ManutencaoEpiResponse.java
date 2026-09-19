package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Registro persistido de manutenção de EPI")
public record ManutencaoEpiResponse(
        Integer id,
        Integer epiId,
        LocalDateTime dataManutencao,
        TipoManutencao tipoManutencao,
        String descricao,
        ResultadoManutencao resultado,
        Integer responsavelId,
        StatusEpi statusAtualEpi
) {
    public static ManutencaoEpiResponse fromOutputDTO(ManutencaoEpiOutputDTO dto) {
        return new ManutencaoEpiResponse(dto.id(), dto.epiId(), dto.dataManutencao(),
                dto.tipoManutencao(), dto.descricao(), dto.resultado(), dto.responsavelId(),
                dto.statusAtualEpi());
    }
}
