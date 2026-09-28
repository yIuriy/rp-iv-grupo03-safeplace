package br.edu.safeplace.backend.application.dto.input;

public record ConfigurarAlertaInputDTO(
        boolean ativo,
        Integer antecedenciaDias
) {
}
