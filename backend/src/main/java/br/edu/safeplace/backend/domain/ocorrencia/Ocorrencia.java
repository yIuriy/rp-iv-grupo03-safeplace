package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

public abstract class Ocorrencia {
    private final Integer idOcorrencia;
    private final LocalDateTime dataOcorrencia;
    private final String local;
    private final String descricao;
    private final PlanoDeAcao planoDeAcao;

    protected Ocorrencia(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local, String descricao,
            PlanoDeAcao planoDeAcao) {

        if (dataOcorrencia == null)
            throw new IllegalArgumentException("Data da ocorrência é obrigatória.");

        if (local == null || local.isBlank())
            throw new IllegalArgumentException("Local é obrigatório.");

        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição é obrigatória.");

        this.idOcorrencia = idOcorrencia;
        this.dataOcorrencia = dataOcorrencia;
        this.local = local;
        this.descricao = descricao;
        this.planoDeAcao = planoDeAcao;
    }

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public LocalDateTime getDataOcorrencia() {
        return dataOcorrencia;
    }

    public String getLocal() {
        return local;
    }

    public String getDescricao() {
        return descricao;
    }

    public PlanoDeAcao getPlanoDeAcao() {
        return planoDeAcao;
    }
}