package br.edu.safeplace.backend.application.dto.input;

import jakarta.validation.constraints.NotNull;

public record AlocarColaboradorInputDTO(
        @NotNull(message = "Identificador do colaborador é obrigatório.")
        Integer colaboradorId,
        String observacao
) {
}
