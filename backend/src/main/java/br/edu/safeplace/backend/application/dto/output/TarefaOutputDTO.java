package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

public record TarefaOutputDTO(
        Integer id,
        String descricao,
        NivelPerigo nivelPerigo,
        boolean classificada,
        LocalDateTime dataClassificacao) {

    public static TarefaOutputDTO deDominio(Tarefa tarefa) {
        return new TarefaOutputDTO(
                tarefa.getId(),
                tarefa.getDescricao(),
                tarefa.getNivelPerigo(),
                tarefa.estaClassificada(),
                tarefa.getDataClassificacao());
    }
}
