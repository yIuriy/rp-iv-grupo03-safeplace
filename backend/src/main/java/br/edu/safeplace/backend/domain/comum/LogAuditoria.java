package br.edu.safeplace.backend.domain.comum;

import java.time.LocalDateTime;

/**
 * Representa um registro de auditoria no sistema (conforme Diagrama de Classes UML).
 */
public class LogAuditoria {

    private final Integer id;
    private final String tipoOperacao;
    private final LocalDateTime dataHora;
    private final String responsavel;
    private final String justificativa;
    private final String entidade;
    private final Integer entidadeId;

    public LogAuditoria(
            Integer id,
            String tipoOperacao,
            LocalDateTime dataHora,
            String responsavel,
            String justificativa,
            String entidade,
            Integer entidadeId) {

        if (tipoOperacao == null || tipoOperacao.isBlank()) {
            throw new IllegalArgumentException("Tipo de operação é obrigatório.");
        }
        if (responsavel == null || responsavel.isBlank()) {
            throw new IllegalArgumentException("Responsável pela operação é obrigatório.");
        }

        this.id = id;
        this.tipoOperacao = tipoOperacao.trim();
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.responsavel = responsavel.trim();
        this.justificativa = justificativa != null ? justificativa.trim() : null;
        this.entidade = entidade != null ? entidade.trim() : null;
        this.entidadeId = entidadeId;
    }

    public static LogAuditoria registrarAlteracao(
            String responsavel,
            String entidade,
            Integer entidadeId,
            String justificativa) {
        return new LogAuditoria(null, "ALTERACAO", LocalDateTime.now(), responsavel, justificativa, entidade, entidadeId);
    }

    public static LogAuditoria registrarCadastro(
            String responsavel,
            String entidade,
            Integer entidadeId,
            String justificativa) {
        return new LogAuditoria(null, "CADASTRO", LocalDateTime.now(), responsavel, justificativa, entidade, entidadeId);
    }

    public static LogAuditoria registrarRemocao(
            Integer entidadeId,
            String responsavel,
            LocalDateTime dataHora,
            String justificativa) {
        return new LogAuditoria(null, "REMOCAO", dataHora, responsavel, justificativa, null, entidadeId);
    }

    public static LogAuditoria registrarRemocao(
            String responsavel,
            String entidade,
            Integer entidadeId,
            String justificativa) {
        return new LogAuditoria(null, "REMOCAO", LocalDateTime.now(), responsavel, justificativa, entidade, entidadeId);
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
}
