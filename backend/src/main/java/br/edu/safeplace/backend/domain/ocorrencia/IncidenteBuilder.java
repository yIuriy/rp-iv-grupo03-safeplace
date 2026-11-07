package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Builder da entidade Incidente (GoF, criacional, #240).
 * Separa a construção de Incidente da sua representação, permitindo compor
 * dados comuns de ocorrência, situação de risco e potencial de dano de forma fluente e nomeada.
 *
 * <p>O Builder não valida: a validação roda na camada validacao (OcorrenciaValidador).
 */
public class IncidenteBuilder {

    private Integer idOcorrencia;
    private String descricao;
    private LocalDateTime dataOcorrencia;
    private LocalDateTime dataRegistro;
    private StatusOcorrencia statusOcorrencia = StatusOcorrencia.ABERTA;
    private Colaborador colaborador;
    private AreaRisco area;
    private Colaborador relator;
    private String localInformado;
    private PlanoDeAcao planoDeAcao;
    private String situacaoRisco;
    private String potencialDano;

    public IncidenteBuilder idOcorrencia(Integer idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
        return this;
    }

    public IncidenteBuilder descricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public IncidenteBuilder dataOcorrencia(LocalDateTime dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
        return this;
    }

    public IncidenteBuilder dataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
        return this;
    }

    public IncidenteBuilder statusOcorrencia(StatusOcorrencia statusOcorrencia) {
        this.statusOcorrencia = statusOcorrencia;
        return this;
    }

    public IncidenteBuilder colaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
        return this;
    }

    public IncidenteBuilder area(AreaRisco area) {
        this.area = area;
        return this;
    }

    public IncidenteBuilder relator(Colaborador relator) {
        this.relator = relator;
        return this;
    }

    public IncidenteBuilder localInformado(String localInformado) {
        this.localInformado = localInformado;
        return this;
    }

    public IncidenteBuilder planoDeAcao(PlanoDeAcao planoDeAcao) {
        this.planoDeAcao = planoDeAcao;
        return this;
    }

    public IncidenteBuilder situacaoRisco(String situacaoRisco) {
        this.situacaoRisco = situacaoRisco;
        return this;
    }

    public IncidenteBuilder potencialDano(String potencialDano) {
        this.potencialDano = potencialDano;
        return this;
    }

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataOcorrencia() {
        return dataOcorrencia;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public StatusOcorrencia getStatusOcorrencia() {
        return statusOcorrencia;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public AreaRisco getArea() {
        return area;
    }

    public Colaborador getRelator() {
        return relator;
    }

    public String getLocalInformado() {
        return localInformado;
    }

    public PlanoDeAcao getPlanoDeAcao() {
        return planoDeAcao;
    }

    public String getSituacaoRisco() {
        return situacaoRisco;
    }

    public String getPotencialDano() {
        return potencialDano;
    }

    public Incidente build() {
        return new Incidente(this);
    }
}
