package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;

import java.time.LocalDate;

public record CriarPlanoDeAcaoRequest(
        String medidasCorretivas,
        String medidasPreventivas,
        LocalDate prazo,
        Integer responsavelColaboradorId,
        Integer responsavelSetorId,
        Boolean alertaAtivo,
        Integer antecedenciaAlertaDias
) {
    public CriarAcaoPlanoInputDTO toInputDTO() {
        return new CriarAcaoPlanoInputDTO(
                medidasCorretivas,
                medidasPreventivas,
                prazo,
                responsavelColaboradorId,
                responsavelSetorId,
                Boolean.TRUE.equals(alertaAtivo),
                antecedenciaAlertaDias
        );
    }
}
