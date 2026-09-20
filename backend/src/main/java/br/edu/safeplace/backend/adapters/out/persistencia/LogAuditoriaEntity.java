package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "logs_auditoria")
@Immutable
public class LogAuditoriaEntity {

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

    public LogAuditoriaEntity() {
    }

    public LogAuditoriaEntity(
            Integer id,
            String tipoOperacao,
            LocalDateTime dataHora,
            String responsavel,
            String justificativa,
            String entidade,
            Integer entidadeId) {
        this.id = id;
        this.tipoOperacao = tipoOperacao;
        this.dataHora = dataHora;
        this.responsavel = responsavel;
        this.justificativa = justificativa;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
    }

    public static LogAuditoriaEntity deDominio(LogAuditoria log) {
        if (log == null) {
            return null;
        }
        return new LogAuditoriaEntity(
                log.getId(),
                log.getTipoOperacao(),
                log.getDataHora(),
                log.getResponsavel(),
                log.getJustificativa(),
                log.getEntidade(),
                log.getEntidadeId()
        );
    }

    public LogAuditoria paraDominio() {
        return new LogAuditoria(
                id,
                tipoOperacao,
                dataHora,
                responsavel,
                justificativa,
                entidade,
                entidadeId
        );
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
