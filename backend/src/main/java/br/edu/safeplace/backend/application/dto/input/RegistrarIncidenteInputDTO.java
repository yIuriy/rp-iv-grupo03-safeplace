package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

import java.time.LocalDateTime;

public record RegistrarIncidenteInputDTO(
        LocalDateTime dataOcorrencia,
        String local,
        String descricao,
        PlanoDeAcao planoDeAcao,
        String situacaoRisco,
        String potencialDano
) {}
