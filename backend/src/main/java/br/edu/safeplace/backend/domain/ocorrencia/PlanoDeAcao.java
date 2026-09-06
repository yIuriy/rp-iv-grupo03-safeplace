package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;

public class PlanoDeAcao {
    private final Integer id;
    private final String medidasCorretivas;
    private final LocalDate prazo;
    private final String status;
    private final String medidasPreventivas;

    public PlanoDeAcao(Integer id, String medidasCorretivas, LocalDate prazo, String status,
            String medidasPreventivas) {
        this.id = id;
        this.medidasCorretivas = medidasCorretivas;
        this.prazo = prazo;
        this.status = status;
        this.medidasPreventivas = medidasPreventivas;
    }

    public Integer getId() {
        return id;
    }

    public String getMedidasCorretivas() {
        return medidasCorretivas;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public String getStatus() {
        return status;
    }

    public String getMedidasPreventivas() {
        return medidasPreventivas;
    }
}