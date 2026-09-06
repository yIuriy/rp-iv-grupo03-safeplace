package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

import java.time.LocalDateTime;

public record RegistrarAcidenteInputDTO(
        LocalDateTime dataOcorrencia,
        String local,
        String descricao,
        PlanoDeAcao planoDeAcao,
        String causaRaiz,
        String tipo,
        String dano,
        String numeroProtocolo,
        String destino
) {}
