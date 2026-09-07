package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

public record PlanoDeAcaoRequest(
        String medidasCorretivas,
        LocalDate prazo,
        String status,
        String medidasPreventivas) {

    public PlanoDeAcao toDomain() {
        return PlanoDeAcao.novo(medidasCorretivas, prazo, status, medidasPreventivas);
    }
}
