package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Builder base para a hierarquia de Ocorrências (GoF, criacional, #240).
 * Reúne os atributos e métodos fluentes comuns a Acidente e Incidente,
 * eliminando construtores telescópicos na superclasse Ocorrencia.
 *
 * <p>O Builder não valida: a validação roda na camada validacao (OcorrenciaValidador).
 *
 * @param <T> O subtipo concreto do builder para chamadas encadeadas fluentes.
 */
public abstract class OcorrenciaBuilder<T extends OcorrenciaBuilder<T>> {

    protected Integer idOcorrencia;
    protected String descricao;
    protected LocalDateTime dataOcorrencia;
    protected LocalDateTime dataRegistro;
    protected StatusOcorrencia statusOcorrencia = StatusOcorrencia.ABERTA;
    protected Colaborador colaborador;
    protected AreaRisco area;
    protected Colaborador relator;
    protected String localInformado;
    protected PlanoDeAcao planoDeAcao;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T idOcorrencia(Integer idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
        return self();
    }

    public T descricao(String descricao) {
        this.descricao = descricao;
        return self();
    }

    public T dataOcorrencia(LocalDateTime dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
        return self();
    }

    public T dataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
        return self();
    }

    public T statusOcorrencia(StatusOcorrencia statusOcorrencia) {
        this.statusOcorrencia = statusOcorrencia;
        return self();
    }

    public T colaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
        return self();
    }

    public T area(AreaRisco area) {
        this.area = area;
        return self();
    }

    public T relator(Colaborador relator) {
        this.relator = relator;
        return self();
    }

    public T localInformado(String localInformado) {
        this.localInformado = localInformado;
        return self();
    }

    public T planoDeAcao(PlanoDeAcao planoDeAcao) {
        this.planoDeAcao = planoDeAcao;
        return self();
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
}
