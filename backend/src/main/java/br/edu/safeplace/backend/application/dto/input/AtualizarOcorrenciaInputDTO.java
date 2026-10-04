package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDateTime;

/**
 * Dados editáveis de uma ocorrência (UC05, cenário alternativo IV). Tipo, data do registro, situação,
 * relator e protocolo CAT não fazem parte da edição e são preservados. Os campos de acidente são
 * ignorados em incidentes e vice-versa.
 */
public record AtualizarOcorrenciaInputDTO(
        LocalDateTime dataOcorrencia,
        String descricao,
        Integer areaId,
        Integer colaboradorId,
        String causaRaiz,
        String gravidade,
        String tipo,
        String dano,
        String destino,
        String situacaoRisco,
        String potencialDano
) {
}
