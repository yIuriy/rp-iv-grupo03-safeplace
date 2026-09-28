package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.ConfigurarAlertaInputDTO;

public record ConfigurarAlertaRequest(
        Boolean ativo,
        Integer antecedenciaDias
) {
    public ConfigurarAlertaInputDTO toInputDTO() {
        return new ConfigurarAlertaInputDTO(Boolean.TRUE.equals(ativo), antecedenciaDias);
    }
}
