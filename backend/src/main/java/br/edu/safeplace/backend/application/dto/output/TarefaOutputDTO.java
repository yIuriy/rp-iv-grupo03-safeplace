package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

public record TarefaOutputDTO(
        Integer id,
        String descricao,
        NivelPerigo nivelPerigo,
        boolean classificada,
        LocalDateTime dataClassificacao,
        Integer responsavelClassificacaoId,
        String responsavelClassificacaoNome,
        Integer areaRiscoId,
        List<Integer> setoresIds) {

    public TarefaOutputDTO(Integer id, String descricao, NivelPerigo nivelPerigo, boolean classificada,
            LocalDateTime dataClassificacao) {
        this(id, descricao, nivelPerigo, classificada, dataClassificacao, null, null, null, List.of());
    }

    public TarefaOutputDTO(Integer id, String descricao, NivelPerigo nivelPerigo, boolean classificada,
            LocalDateTime dataClassificacao, Integer responsavelClassificacaoId,
            String responsavelClassificacaoNome) {
        this(id, descricao, nivelPerigo, classificada, dataClassificacao, responsavelClassificacaoId,
                responsavelClassificacaoNome, null, List.of());
    }

    public static TarefaOutputDTO deDominio(Tarefa tarefa) {
        GestorDeSeguranca responsavel = tarefa.getResponsavelClassificacao();
        return new TarefaOutputDTO(
                tarefa.getId(),
                tarefa.getDescricao(),
                tarefa.getNivelPerigo(),
                tarefa.estaClassificada(),
                tarefa.getDataClassificacao(),
                responsavel != null ? responsavel.getId() : null,
                responsavel != null ? responsavel.getNome() : null,
                tarefa.getAreaRiscoId(),
                tarefa.getSetoresIds());
    }
}
