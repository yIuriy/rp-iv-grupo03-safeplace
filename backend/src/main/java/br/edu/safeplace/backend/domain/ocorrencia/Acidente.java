package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

public class Acidente extends Ocorrencia {
    private final String causaRaiz;
    private final String tipo;
    private final String dano;
    private final String numeroProtocolo;
    private final String destino;

    public Acidente(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local, String descricao,
            PlanoDeAcao planoDeAcao, String causaRaiz, String tipo, String dano,
            String numeroProtocolo, String destino) {
        super(idOcorrencia, dataOcorrencia, local, descricao, planoDeAcao);
        this.causaRaiz = causaRaiz;
        this.tipo = tipo;
        this.dano = dano;
        this.numeroProtocolo = numeroProtocolo;
        this.destino = destino;
    }

    public String getCausaRaiz() {
        return causaRaiz;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDano() {
        return dano;
    }

    public String getNumeroProtocolo() {
        return numeroProtocolo;
    }

    public String getDestino() {
        return destino;
    }
}