package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.ProrrogacaoPrazoOutputDTO;

import java.time.LocalDate;

public record ProrrogacaoPrazoResponse(
        Integer idAcao,
        LocalDate prazoAnterior,
        LocalDate novoPrazo,
        String justificativa
) {
    public static ProrrogacaoPrazoResponse fromOutputDTO(ProrrogacaoPrazoOutputDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ProrrogacaoPrazoResponse(
                dto.idAcao(),
                dto.prazoAnterior(),
                dto.novoPrazo(),
                dto.justificativa()
        );
    }
}
