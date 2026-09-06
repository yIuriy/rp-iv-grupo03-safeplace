package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

public record PlanoDeAcaoResponse(
        Integer id,
        String medidasCorretivas,
        LocalDate prazo,
        String status,
        String medidasPreventivas) {
    public static PlanoDeAcaoResponse fromDomain(PlanoDeAcao plano) {
        if (plano == null)
            return null;

        return new PlanoDeAcaoResponse(
                plano.getId(), plano.getMedidasCorretivas(), plano.getPrazo(), plano.getStatus(),
                plano.getMedidasPreventivas());
    }
}
