package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;
import java.util.Objects;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Alocação confirmada de um colaborador a uma tarefa classificada (RF06, RF10, UC02, UC11, US06, US10).
 * Entidade JPA da tabela tarefas_alocacoes (#260): tarefa, colaborador e responsável são associações por
 * objeto; os getters de id servem ao DTO.
 */
@Entity
@Table(name = "tarefas_alocacoes")
public class AlocacaoTarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(name = "data_alocacao", nullable = false)
    private LocalDateTime dataAlocacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_alocacao_id", nullable = false)
    private Colaborador responsavelAlocacao;

    @Column(columnDefinition = "text")
    private String observacao;

    /** Exclusivo do JPA (decisão #254). */
    protected AlocacaoTarefa() {
    }

    public AlocacaoTarefa(Integer id, Tarefa tarefa, Colaborador colaborador, LocalDateTime dataAlocacao,
                          Colaborador responsavelAlocacao, String observacao) {
        this.id = id;
        this.tarefa = tarefa;
        this.colaborador = colaborador;
        this.dataAlocacao = dataAlocacao != null ? dataAlocacao : LocalDateTime.now();
        this.responsavelAlocacao = responsavelAlocacao;
        this.observacao = observacao != null ? observacao.trim() : null;
    }

    public static AlocacaoTarefa nova(Tarefa tarefa, Colaborador colaborador, Colaborador responsavelAlocacao,
                                      String observacao) {
        return new AlocacaoTarefa(null, tarefa, colaborador, LocalDateTime.now(), responsavelAlocacao, observacao);
    }

    public Integer getId() {
        return id;
    }

    public Tarefa getTarefa() {
        return tarefa;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public Colaborador getResponsavelAlocacao() {
        return responsavelAlocacao;
    }

    public Integer getTarefaId() {
        return tarefa != null ? tarefa.getId() : null;
    }

    public Integer getColaboradorId() {
        return colaborador != null ? colaborador.getId() : null;
    }

    public LocalDateTime getDataAlocacao() {
        return dataAlocacao;
    }

    public Integer getResponsavelAlocacaoId() {
        return responsavelAlocacao != null ? responsavelAlocacao.getId() : null;
    }

    public String getObservacao() {
        return observacao;
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof AlocacaoTarefa alocacao)) {
            return false;
        }
        return getId() != null && getId().equals(alocacao.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
