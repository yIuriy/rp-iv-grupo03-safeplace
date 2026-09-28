package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import jakarta.validation.constraints.NotBlank;

public record AtualizarStatusPlanoRequest(
        @NotBlank(message = "Status é obrigatório.")
        String status
) {
    public AtualizarStatusPlanoInputDTO toInputDTO() {
        return new AtualizarStatusPlanoInputDTO(status);
    }
}
