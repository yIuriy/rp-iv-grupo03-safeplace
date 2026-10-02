package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

public record AlocacaoTarefaOutputDTO(
        Integer id,
        Integer tarefaId,
        String tarefaDescricao,
        Integer colaboradorId,
        String colaboradorNome,
        LocalDateTime dataAlocacao,
        Integer responsavelAlocacaoId,
        String responsavelNome,
        String observacao
) {
    public static AlocacaoTarefaOutputDTO deDominio(AlocacaoTarefa alocacao, String tarefaDescricao,
                                                    String colaboradorNome, String responsavelNome) {
        return new AlocacaoTarefaOutputDTO(
                alocacao.getId(),
                alocacao.getTarefaId(),
                tarefaDescricao,
                alocacao.getColaboradorId(),
                colaboradorNome,
                alocacao.getDataAlocacao(),
                alocacao.getResponsavelAlocacaoId(),
                responsavelNome,
                alocacao.getObservacao()
        );
    }
}
