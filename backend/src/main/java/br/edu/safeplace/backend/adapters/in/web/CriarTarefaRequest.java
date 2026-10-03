package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para cadastro de nova tarefa (UC11)")
public record CriarTarefaRequest(
        @NotBlank(message = "Descrição é obrigatória") @Schema(description = "Descrição da atividade", example = "Solda em altura na estrutura metálica") String descricao,

        @Schema(description = "Grau de periculosidade. Opcional: a classificação pode ser definida depois pelo Gestor de Segurança.", example = "ALTO") NivelPerigo nivelPerigo,

        @Schema(description = "Área de risco opcional vinculada à tarefa", example = "3") Integer areaRiscoId,

        @Schema(description = "Identificadores dos setores físicos vinculados à tarefa", example = "[1, 2]") List<Integer> setoresIds) {

    public CadastrarTarefaInputDTO toInputDTO() {
        return new CadastrarTarefaInputDTO(descricao, nivelPerigo, areaRiscoId, setoresIds);
    }
}
