package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.ProrrogarPrazoInputDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProrrogarPrazoRequest(
        @NotNull(message = "Novo prazo é obrigatório.")
        LocalDate novoPrazo,
        @NotBlank(message = "Justificativa técnica é obrigatória.")
        String justificativa
) {
    public ProrrogarPrazoInputDTO toInputDTO() {
        return new ProrrogarPrazoInputDTO(novoPrazo, justificativa);
    }
}
