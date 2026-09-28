package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

public record ProrrogarPrazoInputDTO(
        LocalDate novoPrazo,
        String justificativa
) {
}
