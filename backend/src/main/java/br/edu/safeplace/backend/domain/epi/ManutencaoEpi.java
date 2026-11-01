package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDateTime;

public class ManutencaoEpi {
    private final Integer id;
    private final Integer epiId;
    private final LocalDateTime dataManutencao;
    private final TipoManutencao tipoManutencao;
    private final String descricao;
    private final ResultadoManutencao resultado;
    private final String responsavelManutencao;
    private final Integer responsavelId;

    public ManutencaoEpi(Integer id, Integer epiId, LocalDateTime dataManutencao,
                         TipoManutencao tipoManutencao, String descricao,
                         ResultadoManutencao resultado, String responsavelManutencao) {
        this(id, epiId, dataManutencao, tipoManutencao, descricao, resultado,
                responsavelManutencao, null);
    }

    public ManutencaoEpi(Integer id, Integer epiId, LocalDateTime dataManutencao,
                         TipoManutencao tipoManutencao, String descricao,
                         ResultadoManutencao resultado, String responsavelManutencao,
                         Integer responsavelId) {
        this.id = id;
        this.epiId = epiId;
        this.dataManutencao = dataManutencao;
        this.tipoManutencao = tipoManutencao;
        this.descricao = descricao;
        this.resultado = resultado;
        this.responsavelManutencao = responsavelManutencao;
        this.responsavelId = responsavelId;
    }

    public static ManutencaoEpi novo(Integer epiId, LocalDateTime dataManutencao,
                                     TipoManutencao tipoManutencao, String descricao,
                                     ResultadoManutencao resultado, String responsavelManutencao) {
        return new ManutencaoEpi(null, epiId, dataManutencao, tipoManutencao, descricao, resultado, responsavelManutencao);
    }

    public static ManutencaoEpi novo(Integer epiId, LocalDateTime dataManutencao,
                                     TipoManutencao tipoManutencao, String descricao,
                                     ResultadoManutencao resultado, String responsavelManutencao,
                                     Integer responsavelId) {
        return new ManutencaoEpi(null, epiId, dataManutencao, tipoManutencao, descricao,
                resultado, responsavelManutencao, responsavelId);
    }

    public Integer getId() {
        return id;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public LocalDateTime getDataManutencao() {
        return dataManutencao;
    }

    public TipoManutencao getTipoManutencao() {
        return tipoManutencao;
    }

    public String getDescricao() {
        return descricao;
    }

    public ResultadoManutencao getResultado() {
        return resultado;
    }

    public String getResponsavelManutencao() {
        return responsavelManutencao;
    }

    public Integer getResponsavelId() {
        return responsavelId;
    }
}
