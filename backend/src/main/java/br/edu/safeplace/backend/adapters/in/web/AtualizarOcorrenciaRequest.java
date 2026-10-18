package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.AtualizarOcorrenciaInputDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Corpo de {@code PUT /api/ocorrencias/{id}} (UC05, cenário alternativo IV). */
public record AtualizarOcorrenciaRequest(
        @NotNull(message = "Data do fato é obrigatória.") LocalDateTime dataOcorrencia,
        @NotBlank(message = "Descrição é obrigatória.") String descricao,
        @NotNull(message = "Área de risco é obrigatória.") Integer areaId,
        @NotNull(message = "Colaborador envolvido é obrigatório.") Integer colaboradorId,

        String causaRaiz,
        String gravidade,
        String tipo,
        String dano,
        String destino,

        String situacaoRisco,
        String potencialDano
) {
    public AtualizarOcorrenciaInputDTO toInputDTO() {
        return new AtualizarOcorrenciaInputDTO(
                dataOcorrencia,
                descricao,
                areaId,
                colaboradorId,
                causaRaiz,
                gravidade,
                tipo,
                dano,
                destino,
                situacaoRisco,
                potencialDano
        );
    }
}
