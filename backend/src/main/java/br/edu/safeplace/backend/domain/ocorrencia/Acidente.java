package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.Arrays;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Acidente de trabalho (RF01, RF02, RF13). Causa raiz e gravidade são obrigatórias e nunca assumem
 * valor padrão (issue #195). {@code tipo}, {@code dano} e {@code destino} são complementos textuais
 * opcionais herdados da modelagem anterior, fora do recorte mínimo do relato (DS09, seção 2.9).
 */
public class Acidente extends Ocorrencia {
    private final CausaRaizAcidente causaRaiz;
    private final String tipo;
    private final String dano;
    /** Value Object guardado como atributo (issue #230); {@code null} enquanto a CAT não é emitida. */
    private final NumeroProtocoloCAT numeroProtocoloCAT;
    private final String destino;
    private final NivelPerigo gravidade;

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

    public static Acidente novo(
            String descricao,
            LocalDateTime dataOcorrencia,
            AreaRisco area,
            Colaborador colaborador,
            Colaborador relator,
            CausaRaizAcidente causaRaiz,
            String tipo,
            String dano,
            String destino,
            NivelPerigo gravidade) {
        return new Acidente(
                null,
                descricao,
                dataOcorrencia,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                relator,
                null,
                null,
                causaRaiz,
                tipo,
                dano,
                null,
                destino,
                gravidade
        );
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
