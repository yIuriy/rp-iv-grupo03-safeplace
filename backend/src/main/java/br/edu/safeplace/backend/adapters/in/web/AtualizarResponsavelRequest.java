package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;

public record AtualizarResponsavelRequest(
        Integer colaboradorId,
        Integer setorId
) {
    public AtualizarResponsavelInputDTO toInputDTO() {
        return new AtualizarResponsavelInputDTO(colaboradorId, setorId);
    }
}
