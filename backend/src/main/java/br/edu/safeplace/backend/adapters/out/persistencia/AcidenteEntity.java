package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;

@Entity
@Table(name = "acidentes")
public class AcidenteEntity extends OcorrenciaEntity {
    @Column(name = "causa_raiz")
    private String causaRaiz;

    private String tipo;
    private String dano;

    /** Value Object embutido na mesma coluna de antes (issue #230); nulo enquanto a CAT não é emitida. */
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "numero_protocolo", length = 80))
    private NumeroProtocoloCAT numeroProtocolo;

    private String destino;

    @Column(name = "gravidade", nullable = false, length = 20)
    private String gravidade;

    protected AcidenteEntity() {
    }

    public AcidenteEntity(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            String statusOcorrencia,
            AreaRiscoEntity area,
            ColaboradorEntidade colaborador,
            ColaboradorEntidade relator,
            String local,
            String descricao,
            PlanoDeAcaoEntity planoDeAcao,
            String causaRaiz,
            String tipo,
            String dano,
            NumeroProtocoloCAT numeroProtocolo,
            String destino,
            String gravidade) {
        super(idOcorrencia, dataOcorrencia, dataRegistro, statusOcorrencia, area, colaborador, relator, local, descricao, planoDeAcao);
        this.causaRaiz = causaRaiz;
        this.tipo = tipo;
        this.dano = dano;
        this.numeroProtocolo = numeroProtocolo;
        this.destino = destino;
        this.gravidade = gravidade;
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

    public NumeroProtocoloCAT getNumeroProtocolo() {
        return numeroProtocolo;
    }

    public String getDestino() {
        return destino;
    }

    public String getGravidade() {
        return gravidade;
    }
}