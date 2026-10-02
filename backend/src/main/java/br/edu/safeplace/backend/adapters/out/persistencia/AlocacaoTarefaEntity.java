package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarefas_alocacoes")
public class AlocacaoTarefaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tarefa_id", nullable = false)
    private Integer tarefaId;

    @Column(name = "colaborador_id", nullable = false)
    private Integer colaboradorId;

    @Column(name = "data_alocacao", nullable = false)
    private LocalDateTime dataAlocacao;

    @Column(name = "responsavel_alocacao_id", nullable = false)
    private Integer responsavelAlocacaoId;

    @Column(columnDefinition = "text")
    private String observacao;

    public AlocacaoTarefaEntity() {
    }

    public AlocacaoTarefaEntity(Integer id, Integer tarefaId, Integer colaboradorId,
                                LocalDateTime dataAlocacao, Integer responsavelAlocacaoId, String observacao) {
        this.id = id;
        this.tarefaId = tarefaId;
        this.colaboradorId = colaboradorId;
        this.dataAlocacao = dataAlocacao;
        this.responsavelAlocacaoId = responsavelAlocacaoId;
        this.observacao = observacao;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTarefaId() {
        return tarefaId;
    }

    public Integer getColaboradorId() {
        return colaboradorId;
    }

    public LocalDateTime getDataAlocacao() {
        return dataAlocacao;
    }

    public Integer getResponsavelAlocacaoId() {
        return responsavelAlocacaoId;
    }

    public String getObservacao() {
        return observacao;
    }
}
