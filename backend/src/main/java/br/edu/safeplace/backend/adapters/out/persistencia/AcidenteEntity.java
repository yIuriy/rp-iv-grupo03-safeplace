package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "acidentes")
public class AcidenteEntity extends OcorrenciaEntity {
    @Column(name = "causa_raiz")
    private String causaRaiz;

    private String tipo;
    private String dano;

    @Column(name = "numero_protocolo")
    private String numeroProtocolo;

    private String destino;

    protected AcidenteEntity() {
    }

    public AcidenteEntity(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local, String descricao,
            PlanoDeAcaoEntity planoDeAcao, String causaRaiz, String tipo, String dano,
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