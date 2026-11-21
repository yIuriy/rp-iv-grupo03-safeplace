package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Builder da entidade {@link PlanoDeAcao} (GoF, criacional, #262).
 * Separa a construção da ação do plano sem parâmetros posicionais nem métodos estáticos de criação.
 *
 * <p>O Builder não valida: as regras de definição de ação e plano rodam em
 * {@code AcaoPlanoValidador} e {@code PlanoValidador} antes do repositório (#266).</p>
 */
public class PlanoDeAcaoBuilder {

    private Integer id;
    private Ocorrencia ocorrencia;
    private String medidasCorretivas;
    private String medidasPreventivas;
    private LocalDate prazo;
    private String status = StatusPlanoDeAcao.ABERTA.name();
    private Colaborador responsavelColaborador;
    private Setor responsavelSetor;
    private boolean alertaAtivo;
    private Integer antecedenciaAlertaDias;
    private GestorDeSeguranca gestor;

    public PlanoDeAcaoBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public PlanoDeAcaoBuilder ocorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
        return this;
    }

    public PlanoDeAcaoBuilder medidasCorretivas(String medidasCorretivas) {
        this.medidasCorretivas = medidasCorretivas;
        return this;
    }

    public PlanoDeAcaoBuilder medidasPreventivas(String medidasPreventivas) {
        this.medidasPreventivas = medidasPreventivas;
        return this;
    }

    public PlanoDeAcaoBuilder prazo(LocalDate prazo) {
        this.prazo = prazo;
        return this;
    }

    public PlanoDeAcaoBuilder status(String status) {
        this.status = status;
        return this;
    }

    public PlanoDeAcaoBuilder responsavelColaborador(Colaborador responsavelColaborador) {
        this.responsavelColaborador = responsavelColaborador;
        return this;
    }

    public PlanoDeAcaoBuilder responsavelSetor(Setor responsavelSetor) {
        this.responsavelSetor = responsavelSetor;
        return this;
    }

    public PlanoDeAcaoBuilder alertaAtivo(boolean alertaAtivo) {
        this.alertaAtivo = alertaAtivo;
        return this;
    }

    public PlanoDeAcaoBuilder antecedenciaAlertaDias(Integer antecedenciaAlertaDias) {
        this.antecedenciaAlertaDias = antecedenciaAlertaDias;
        return this;
    }

    public PlanoDeAcaoBuilder gestor(GestorDeSeguranca gestor) {
        this.gestor = gestor;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public String getMedidasCorretivas() {
        return medidasCorretivas;
    }

    public String getMedidasPreventivas() {
        return medidasPreventivas;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public String getStatus() {
        return status;
    }

    public Colaborador getResponsavelColaborador() {
        return responsavelColaborador;
    }

    public Setor getResponsavelSetor() {
        return responsavelSetor;
    }

    public boolean isAlertaAtivo() {
        return alertaAtivo;
    }

    public Integer getAntecedenciaAlertaDias() {
        return antecedenciaAlertaDias;
    }

    public GestorDeSeguranca getGestor() {
        return gestor;
    }

    public PlanoDeAcao build() {
        return new PlanoDeAcao(this);
    }
}
