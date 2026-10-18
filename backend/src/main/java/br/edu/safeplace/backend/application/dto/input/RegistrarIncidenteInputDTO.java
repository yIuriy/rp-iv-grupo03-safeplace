package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDateTime;

/** Dados do relato de incidente (UC09). O relator é definido pelo servidor (issue #195). */
public record RegistrarIncidenteInputDTO(
        LocalDateTime dataOcorrencia,
        String descricao,
        String situacaoRisco,
        String potencialDano,
        Integer areaId,
        Integer colaboradorId
) {
}
