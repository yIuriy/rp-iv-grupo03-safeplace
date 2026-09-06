package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

public class Incidente extends Ocorrencia {
    private final String situacaoRisco;
    private final String potencialDano;

    public Incidente(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local, String descricao,
            PlanoDeAcao planoDeAcao, String situacaoRisco, String potencialDano) {
        super(idOcorrencia, dataOcorrencia, local, descricao, planoDeAcao);
        this.situacaoRisco = situacaoRisco;
        this.potencialDano = potencialDano;
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }
}