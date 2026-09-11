package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;

import java.time.LocalDateTime;

public record ConcluirManutencaoInputDTO(
        Integer epiId,
        LocalDateTime dataManutencao,
        TipoManutencao tipoManutencao,
        String descricao,
        ResultadoManutencao resultado,
        String responsavelManutencao
) {}
