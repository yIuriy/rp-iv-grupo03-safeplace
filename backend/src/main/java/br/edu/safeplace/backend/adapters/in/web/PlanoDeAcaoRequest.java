package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

public record PlanoDeAcaoRequest(
        String medidasCorretivas,
        LocalDate prazo,
        String status,
        String medidasPreventivas) {
}
