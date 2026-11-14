package br.edu.safeplace.backend.domain.comum;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa um registro de auditoria no sistema (conforme Diagrama de Classes UML). É a entidade JPA da
 * tabela logs_auditoria (#256), só de inserção ({@link Immutable}, RNF05). {@code entidadeId} continua um id,
 * e não uma associação, porque a trilha referencia qualquer entidade auditada (decisão #254, seção 2.2).
 */
@Entity
@Table(name = "logs_auditoria")
@Immutable
public class LogAuditoria {

    public static final String CADASTRO = "CADASTRO";
    public static final String ALTERACAO = "ALTERACAO";
    public static final String REMOCAO = "REMOCAO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo_operacao", nullable = false, length = 50)
    private String tipoOperacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 160)
    private String responsavel;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(length = 100)
    private String entidade;

    @Column(name = "entidade_id")
    private Integer entidadeId;

    /** Exclusivo do JPA (decisão #254). */
    protected LogAuditoria() {
    }

    /**
     * Monta o registro sem validar. O tipo de operação e o responsável obrigatórios são conferidos pelo
     * {@code LogAuditoriaValidador} antes de gravar (Chain of Responsibility, #297).
     */
    public LogAuditoria(
            Integer id,
            String tipoOperacao,
            LocalDateTime dataHora,
            String responsavel,
            String justificativa,
            String entidade,
            Integer entidadeId) {

        this.id = id;
        this.tipoOperacao = tipoOperacao != null ? tipoOperacao.trim() : null;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.responsavel = responsavel != null ? responsavel.trim() : null;
        this.justificativa = justificativa != null ? justificativa.trim() : null;
        this.entidade = entidade != null ? entidade.trim() : null;
        this.entidadeId = entidadeId;
    }

    public static LogAuditoria registrarAlteracao(
            String responsavel,
            String entidade,
            Integer entidadeId,
            String justificativa) {
        return new LogAuditoria(null, ALTERACAO, LocalDateTime.now(), responsavel, justificativa, entidade, entidadeId);
    }

    public static LogAuditoria registrarCadastro(
            String responsavel,
            String entidade,
            Integer entidadeId,
            String justificativa) {
        return new LogAuditoria(null, CADASTRO, LocalDateTime.now(), responsavel, justificativa, entidade, entidadeId);
    }

    public static LogAuditoria registrarRemocao(
            Integer entidadeId,
            String responsavel,
            LocalDateTime dataHora,
            String justificativa) {
        return new LogAuditoria(null, REMOCAO, dataHora, responsavel, justificativa, null, entidadeId);
    }

    public static LogAuditoria registrarRemocao(
            String responsavel,
            String entidade,
            Integer entidadeId,
            String justificativa) {
        return new LogAuditoria(null, REMOCAO, LocalDateTime.now(), responsavel, justificativa, entidade, entidadeId);
    }

    public Integer getId() {
        return id;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getEntidade() {
        return entidade;
    }

    public Integer getEntidadeId() {
        return entidadeId;
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof LogAuditoria log)) {
            return false;
        }
        return getId() != null && getId().equals(log.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
