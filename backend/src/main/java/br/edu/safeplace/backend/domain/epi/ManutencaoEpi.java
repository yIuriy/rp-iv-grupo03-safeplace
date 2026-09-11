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

    public ManutencaoEpi(Integer id, Integer epiId, LocalDateTime dataManutencao,
                         TipoManutencao tipoManutencao, String descricao,
                         ResultadoManutencao resultado, String responsavelManutencao) {
        if (epiId == null) {
            throw new IllegalArgumentException("Identificador do EPI é obrigatório.");
        }
        if (dataManutencao == null) {
            throw new IllegalArgumentException("Data da manutenção é obrigatória.");
        }
        if (tipoManutencao == null) {
            throw new IllegalArgumentException("Tipo de manutenção é obrigatório.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da manutenção é obrigatória.");
        }
        if (resultado == null) {
            throw new IllegalArgumentException("Resultado da manutenção é obrigatório.");
        }
        if (responsavelManutencao == null || responsavelManutencao.isBlank()) {
            throw new IllegalArgumentException("Responsável pela manutenção é obrigatório.");
        }

        this.id = id;
        this.epiId = epiId;
        this.dataManutencao = dataManutencao;
        this.tipoManutencao = tipoManutencao;
        this.descricao = descricao;
        this.resultado = resultado;
        this.responsavelManutencao = responsavelManutencao;
    }

    public static ManutencaoEpi novo(Integer epiId, LocalDateTime dataManutencao,
                                     TipoManutencao tipoManutencao, String descricao,
                                     ResultadoManutencao resultado, String responsavelManutencao) {
        return new ManutencaoEpi(null, epiId, dataManutencao, tipoManutencao, descricao, resultado, responsavelManutencao);
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
}
