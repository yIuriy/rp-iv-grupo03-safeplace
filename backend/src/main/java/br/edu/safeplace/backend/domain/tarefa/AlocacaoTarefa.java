package br.edu.safeplace.backend.domain.tarefa;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa a alocação confirmada de um colaborador a uma tarefa classificada
 * (RF06, RF10, UC02, UC11, US06, US10).
 */
public class AlocacaoTarefa {

    private final Integer id;
    private final Integer tarefaId;
    private final Integer colaboradorId;
    private final LocalDateTime dataAlocacao;
    private final Integer responsavelAlocacaoId;
    private final String observacao;

    public AlocacaoTarefa(Integer id, Integer tarefaId, Integer colaboradorId,
                          LocalDateTime dataAlocacao, Integer responsavelAlocacaoId, String observacao) {
        this.id = id;
        this.tarefaId = tarefaId;
        this.colaboradorId = colaboradorId;
        this.dataAlocacao = dataAlocacao != null ? dataAlocacao : LocalDateTime.now();
        this.responsavelAlocacaoId = responsavelAlocacaoId;
        this.observacao = observacao != null ? observacao.trim() : null;
    }

    public static AlocacaoTarefa nova(Integer tarefaId, Integer colaboradorId,
                                      Integer responsavelAlocacaoId, String observacao) {
        return new AlocacaoTarefa(null, tarefaId, colaboradorId, LocalDateTime.now(), responsavelAlocacaoId, observacao);
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
