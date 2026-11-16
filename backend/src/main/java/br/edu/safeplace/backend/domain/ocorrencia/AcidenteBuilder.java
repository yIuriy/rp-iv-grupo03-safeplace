package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/**
 * Builder da entidade Acidente (GoF, criacional, #240).
 * Separa a construção de Acidente da sua representação, compondo
 * dados comuns de ocorrência (via {@link OcorrenciaBuilder}) e atributos próprios
 * de acidente (causa raiz, CAT, gravidade, tipo, dano, destino) de forma fluente e nomeada.
 *
 * <p>O Builder não valida: a validação roda na camada validacao (OcorrenciaValidador).
 */
public class AcidenteBuilder extends OcorrenciaBuilder<AcidenteBuilder> {

    private CausaRaizAcidente causaRaiz;
    private String tipo;
    private String dano;
    private NumeroProtocoloCAT numeroProtocoloCAT;
    private String destino;
    private NivelPerigo gravidade;

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
