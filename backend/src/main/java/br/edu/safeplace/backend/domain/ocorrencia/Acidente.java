package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.Arrays;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Acidente de trabalho (RF01, RF02, RF13). É a subclasse JOINED da tabela {@code acidentes} (#261).
 * Causa raiz e gravidade são obrigatórias e nunca assumem valor padrão (issue #195).
 */
@Entity
@Table(name = "acidentes")
public class Acidente extends Ocorrencia {

    @Enumerated(EnumType.STRING)
    @Column(name = "causa_raiz")
    private CausaRaizAcidente causaRaiz;

    @Column
    private String tipo;

    @Column
    private String dano;

    /** Value Object guardado como atributo (issue #230); {@code null} enquanto a CAT não é emitida. */
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "numero_protocolo", length = 80))
    private NumeroProtocoloCAT numeroProtocoloCAT;

    @Column
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravidade", nullable = false, length = 20)
    private NivelPerigo gravidade;

    /** Exclusivo do JPA (decisão #254). */
    protected Acidente() {
    }

    /** Construtor a partir do Builder (GoF, criacional, #240). */
    public Acidente(AcidenteBuilder builder) {
        super(builder);
        NumeroProtocoloCAT protocolo = builder.getNumeroProtocoloCAT();
        this.numeroProtocoloCAT = protocolo != null && !protocolo.semValor() ? protocolo : null;
        this.causaRaiz = builder.getCausaRaiz();
        this.tipo = builder.getTipo() != null ? builder.getTipo().trim() : null;
        this.dano = builder.getDano() != null ? builder.getDano().trim() : null;
        this.destino = builder.getDestino() != null ? builder.getDestino().trim() : null;
        this.gravidade = builder.getGravidade();
    }

    public static AcidenteBuilder builder() {
        return new AcidenteBuilder();
    }

    public static class Builder extends AcidenteBuilder {
    }

    @Override
    public TipoOcorrencia getTipoOcorrencia() {
        return TipoOcorrencia.ACIDENTE;
    }

    /**
     * Lê a gravidade informada pelo cliente na escala {@link NivelPerigo}. Valor ausente ou
     * desconhecido é recusado, nunca vira {@code BAIXO} (issue #195).
     */
    public static NivelPerigo lerGravidade(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DadosOcorrenciaInvalidosException(
                    "Gravidade é obrigatória para acidentes: BAIXO, MEDIO, ALTO ou CRITICO.");
        }
        return Arrays.stream(NivelPerigo.values())
                .filter(nivel -> nivel.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new DadosOcorrenciaInvalidosException(
                        "Gravidade inválida: " + valor.trim() + ". Use BAIXO, MEDIO, ALTO ou CRITICO."));
    }

    public Acidente consolidarCAT(long sequencial) {
        if (this.numeroProtocoloCAT != null) {
            throw new IllegalStateException("Protocolo CAT já emitido e não pode ser alterado.");
        }
        NumeroProtocoloCAT protocolo = NumeroProtocoloCAT.gerar(this.getDataOcorrencia(), sequencial);
        return new AcidenteBuilder()
                .idOcorrencia(this.getIdOcorrencia())
                .descricao(this.getDescricao())
                .dataOcorrencia(this.getDataOcorrencia())
                .dataRegistro(this.getDataRegistro())
                .statusOcorrencia(this.getStatusOcorrencia())
                .colaborador(this.getColaborador())
                .area(this.getArea())
                .relator(this.getRelator())
                .localInformado(this.getLocalInformado())
                .planoDeAcao(this.getPlanoDeAcao())
                .causaRaiz(this.causaRaiz)
                .tipo(this.tipo)
                .dano(this.dano)
                .numeroProtocoloCAT(protocolo)
                .destino(this.destino)
                .gravidade(this.gravidade)
                .build();
    }

    public NivelPerigo getGravidade() {
        return gravidade;
    }

    @Override
    public boolean isGrave() {
        return gravidade.isPeloMenos(NivelPerigo.ALTO);
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

    /** O Value Object do protocolo, ou {@code null} enquanto a CAT não é emitida. */
    public NumeroProtocoloCAT getProtocoloCAT() {
        return numeroProtocoloCAT;
    }

    /** Texto do protocolo, como antes da issue #230 (contrato HTTP e banco inalterados). */
    public String getNumeroProtocoloCAT() {
        return numeroProtocoloCAT != null ? numeroProtocoloCAT.getValor() : null;
    }

    public String getNumeroProtocolo() {
        return getNumeroProtocoloCAT();
    }

    public String getDestino() {
        return destino;
    }
}
