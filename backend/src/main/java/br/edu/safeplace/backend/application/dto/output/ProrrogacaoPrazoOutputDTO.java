package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;

import java.time.LocalDate;

public record ProrrogacaoPrazoOutputDTO(
        Integer idAcao,
        LocalDate prazoAnterior,
        LocalDate novoPrazo,
        String justificativa
) {
    public static ProrrogacaoPrazoOutputDTO deDominio(ProrrogacaoPrazo p) {
        if (p == null) {
            return null;
        }
        return new ProrrogacaoPrazoOutputDTO(
                p.idAcao(),
                p.prazoAnterior(),
                p.novoPrazo(),
                p.justificativa()
        );
    }
}
