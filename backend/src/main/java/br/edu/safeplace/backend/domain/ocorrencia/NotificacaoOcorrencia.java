package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Notificação interna ao Gestor de Segurança sobre um novo relato (UC09, passo 12; DS09,
 * {@code VisaoNotificacao.exibirNotificacao(protocolo, tipo, setor)}). Fica gravada para consulta
 * (issue #195); o canal é interno, sem e-mail nem SMS.
 * É a entidade JPA da tabela notificacoes_ocorrencia (#261), só de inserção ({@link Immutable}, RNF05).
 */
@Entity
@Table(name = "notificacoes_ocorrencia")
@Immutable
public class NotificacaoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ocorrencia_id", nullable = false)
    private Integer ocorrenciaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ocorrencia", nullable = false, length = 20)
    private TipoOcorrencia tipoOcorrencia;

    /** Value Object embutido na mesma coluna de antes (issue #230); nulo para incidentes. */
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "numero_protocolo_cat", length = 80))
    private NumeroProtocoloCAT numeroProtocoloCAT;

    @Column(name = "area_nome", length = 160)
    private String areaNome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    /** Exclusivo do JPA (decisão #254). */
    protected NotificacaoOcorrencia() {
    }

    public NotificacaoOcorrencia(
            Integer id,
            Integer ocorrenciaId,
            TipoOcorrencia tipoOcorrencia,
            NumeroProtocoloCAT numeroProtocoloCAT,
            String areaNome,
            String mensagem,
            LocalDateTime criadaEm) {
        this.id = id;
        this.ocorrenciaId = ocorrenciaId;
        this.tipoOcorrencia = tipoOcorrencia;
        this.numeroProtocoloCAT = numeroProtocoloCAT;
        this.areaNome = areaNome;
        this.mensagem = mensagem;
        this.criadaEm = criadaEm != null ? criadaEm : LocalDateTime.now();
    }

    public static NotificacaoOcorrencia deNovoRelato(Ocorrencia ocorrencia) {
        // A ocorrência é validada por NotificacaoOcorrenciaValidador no adaptador (#266).
        TipoOcorrencia tipo = ocorrencia instanceof Acidente ? TipoOcorrencia.ACIDENTE : TipoOcorrencia.INCIDENTE;
        // Value Object (#230): usa o protocolo guardado no acidente, sem recriar nem revalidar.
        NumeroProtocoloCAT protocolo = ocorrencia instanceof Acidente acidente ? acidente.getProtocoloCAT() : null;
        String area = ocorrencia.getArea() != null ? ocorrencia.getArea().getNome() : null;

        StringBuilder mensagem = new StringBuilder(tipo == TipoOcorrencia.ACIDENTE ? "Novo acidente" : "Novo incidente")
                .append(" registrado");
        if (protocolo != null) {
            mensagem.append(" (protocolo ").append(protocolo).append(")");
        }
        if (area != null) {
            mensagem.append(" na área ").append(area);
        }
        mensagem.append(". Aguarda triagem.");

        return new NotificacaoOcorrencia(null, ocorrencia.getId(), tipo, protocolo, area, mensagem.toString(),
                LocalDateTime.now());
    }

    public Integer id() {
        return id;
    }

    public Integer ocorrenciaId() {
        return ocorrenciaId;
    }

    public TipoOcorrencia tipoOcorrencia() {
        return tipoOcorrencia;
    }

    public NumeroProtocoloCAT numeroProtocoloCAT() {
        return numeroProtocoloCAT;
    }

    public String areaNome() {
        return areaNome;
    }

    public String mensagem() {
        return mensagem;
    }

    public LocalDateTime criadaEm() {
        return criadaEm;
    }

    public Integer getId() {
        return id;
    }

    public Integer getOcorrenciaId() {
        return ocorrenciaId;
    }

    public TipoOcorrencia getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public NumeroProtocoloCAT getNumeroProtocoloCAT() {
        return numeroProtocoloCAT;
    }

    public String getAreaNome() {
        return areaNome;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof NotificacaoOcorrencia that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
