package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDateTime;

/**
 * Dados do relato de acidente (UC09). Não há protocolo, relator nem plano de ação: o protocolo CAT e
 * o relator são definidos pelo servidor (issue #195).
 */
public record RegistrarAcidenteInputDTO(
        LocalDateTime dataOcorrencia,
        String descricao,
        String causaRaiz,
        String gravidade,
        String tipo,
        String dano,
        String destino,
        Integer areaId,
        Integer colaboradorId
) {
}
