package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de uma tarefa e de sua classificação de periculosidade")
public record TarefaResponse(
        @Schema(description = "Identificador único da tarefa", example = "1") Integer id,

        @Schema(description = "Descrição da atividade", example = "Solda em altura na estrutura metálica") String descricao,

        @Schema(description = "Grau de periculosidade atribuído", example = "ALTO") NivelPerigo nivelPerigo,

        @Schema(description = "Indica se a tarefa já possui grau de risco atribuído", example = "true") boolean classificada,

        @Schema(description = "Data e hora da última classificação", example = "2026-09-13T10:15:30") LocalDateTime dataClassificacao) {

    public static TarefaResponse fromOutputDTO(TarefaOutputDTO dto) {
        return new TarefaResponse(
                dto.id(),
                dto.descricao(),
                dto.nivelPerigo(),
                dto.classificada(),
                dto.dataClassificacao());
    }
}
