package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Builder da entidade Acidente (GoF, criacional, #240).
 * Separa a construção de Acidente da sua representação, permitindo compor
 * dados comuns de ocorrência, causa raiz, CAT e gravidade de forma fluente e nomeada.
 *
 * <p>O Builder não valida: a validação roda na camada validacao (OcorrenciaValidador).
 */
public class AcidenteBuilder {

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
    private CausaRaizAcidente causaRaiz;
    private String tipo;
    private String dano;
    private NumeroProtocoloCAT numeroProtocoloCAT;
    private String destino;
    private NivelPerigo gravidade;

    public AcidenteBuilder idOcorrencia(Integer idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
        return this;
    }

    public AcidenteBuilder descricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public AcidenteBuilder dataOcorrencia(LocalDateTime dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
        return this;
    }

    public AcidenteBuilder dataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
        return this;
    }

    public AcidenteBuilder statusOcorrencia(StatusOcorrencia statusOcorrencia) {
        this.statusOcorrencia = statusOcorrencia;
        return this;
    }

    public AcidenteBuilder colaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
        return this;
    }

    public AcidenteBuilder area(AreaRisco area) {
        this.area = area;
        return this;
    }

    public AcidenteBuilder relator(Colaborador relator) {
        this.relator = relator;
        return this;
    }

    public AcidenteBuilder localInformado(String localInformado) {
        this.localInformado = localInformado;
        return this;
    }

    public AcidenteBuilder planoDeAcao(PlanoDeAcao planoDeAcao) {
        this.planoDeAcao = planoDeAcao;
        return this;
    }

    public AcidenteBuilder causaRaiz(CausaRaizAcidente causaRaiz) {
        this.causaRaiz = causaRaiz;
        return this;
    }

    public AcidenteBuilder tipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public AcidenteBuilder dano(String dano) {
        this.dano = dano;
        return this;
    }

    public AcidenteBuilder numeroProtocoloCAT(NumeroProtocoloCAT numeroProtocoloCAT) {
        this.numeroProtocoloCAT = numeroProtocoloCAT;
        return this;
    }

    public AcidenteBuilder destino(String destino) {
        this.destino = destino;
        return this;
    }

    public AcidenteBuilder gravidade(NivelPerigo gravidade) {
        this.gravidade = gravidade;
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

    public CausaRaizAcidente getCausaRaiz() {
        return causaRaiz;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDano() {
        return dano;
    }

    public NumeroProtocoloCAT getNumeroProtocoloCAT() {
        return numeroProtocoloCAT;
    }

    public String getDestino() {
        return destino;
    }

    public NivelPerigo getGravidade() {
        return gravidade;
    }

    public Acidente build() {
        return new Acidente(this);
    }
}
