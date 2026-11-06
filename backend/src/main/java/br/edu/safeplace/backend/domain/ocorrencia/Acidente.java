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
public non-sealed class Acidente extends Ocorrencia {

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

    public Acidente(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            String local,
            PlanoDeAcao planoDeAcao,
            CausaRaizAcidente causaRaiz,
            String tipo,
            String dano,
            NumeroProtocoloCAT numeroProtocoloCAT,
            String destino,
            NivelPerigo gravidade) {
        super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                colaborador, area, relator, local, planoDeAcao);

        // Causa raiz, gravidade e formato do protocolo: OcorrenciaValidador (issue #266).

        // Value Object (#230): o protocolo é guardado como veio, sem revalidar; sem texto, não foi emitido.
        this.numeroProtocoloCAT = numeroProtocoloCAT != null && !numeroProtocoloCAT.semValor()
                ? numeroProtocoloCAT
                : null;

        this.causaRaiz = causaRaiz;
        this.tipo = tipo != null ? tipo.trim() : null;
        this.dano = dano != null ? dano.trim() : null;
        this.destino = destino != null ? destino.trim() : null;
        this.gravidade = gravidade;
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
        return new Acidente(
                this.getIdOcorrencia(),
                this.getDescricao(),
                this.getDataOcorrencia(),
                this.getDataRegistro(),
                this.getStatusOcorrencia(),
                this.getColaborador(),
                this.getArea(),
                this.getRelator(),
                this.getLocalInformado(),
                this.getPlanoDeAcao(),
                this.causaRaiz,
                this.tipo,
                this.dano,
                protocolo,
                this.destino,
                this.gravidade
        );
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
