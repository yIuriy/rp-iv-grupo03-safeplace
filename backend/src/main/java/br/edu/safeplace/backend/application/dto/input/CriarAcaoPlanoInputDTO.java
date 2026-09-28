package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

public record CriarAcaoPlanoInputDTO(
        String medidasCorretivas,
        String medidasPreventivas,
        LocalDate prazo,
        Integer responsavelColaboradorId,
        Integer responsavelSetorId,
        boolean alertaAtivo,
        Integer antecedenciaAlertaDias
) {
}
