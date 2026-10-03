package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de uma tarefa e de sua classificação de periculosidade")
public record TarefaResponse(
        @Schema(description = "Identificador único da tarefa", example = "1") Integer id,

        @Schema(description = "Descrição da atividade", example = "Solda em altura na estrutura metálica") String descricao,

        @Schema(description = "Grau de periculosidade atribuído (vocabulário único NivelPerigo)", example = "ALTO") NivelPerigo nivelPerigo,

        @Schema(description = "Indica se a tarefa já possui grau de risco atribuído", example = "true") boolean classificada,

        @Schema(description = "Data e hora da última classificação", example = "2026-09-13T10:15:30") LocalDateTime dataClassificacao,

        @Schema(description = "Identificador do Gestor de Segurança responsável técnico pela última classificação. Nulo em classificações anteriores ao registro de autoria.", example = "1") Integer responsavelClassificacaoId,

        @Schema(description = "Nome do responsável técnico pela última classificação", example = "Gestor Demonstração") String responsavelClassificacaoNome,

        @Schema(description = "Área de risco opcional vinculada à tarefa", example = "3") Integer areaRiscoId,

        @Schema(description = "Setores físicos vinculados à tarefa", example = "[1, 2]") List<Integer> setoresIds) {

    public static TarefaResponse fromOutputDTO(TarefaOutputDTO dto) {
        return new TarefaResponse(
                dto.id(),
                dto.descricao(),
                dto.nivelPerigo(),
                dto.classificada(),
                dto.dataClassificacao(),
                dto.responsavelClassificacaoId(),
                dto.responsavelClassificacaoNome(),
                dto.areaRiscoId(),
                dto.setoresIds());
    }
}
