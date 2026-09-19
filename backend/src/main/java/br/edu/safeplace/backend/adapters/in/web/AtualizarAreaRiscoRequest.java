package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para atualização cadastral de área de risco (UC03)")
public record AtualizarAreaRiscoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome da área de risco", example = "Linha de Produção")
        String nome,

        @Schema(description = "Descrição detalhada da área de risco", example = "Setor de solda e montagem")
        String descricao,

        @NotNull(message = "Nível de perigo é obrigatório")
        @Schema(description = "Grau de perigo da área", example = "ALTO")
        NivelPerigo nivelPerigo,

        @Schema(description = "ID do setor físico ao qual a área pertence", example = "1")
        Integer setorId,

        @Schema(description = "Lista de agentes de risco identificados", example = "[\"Ruído\", \"Calor\", \"Partículas\"]")
        List<String> agentesRisco,

        @Schema(description = "Limites físicos ou operacionais mapeados", example = "Delimitação entre linhas A e B")
        String limites,

        @NotEmpty(message = "A área de risco exige ao menos um EPI obrigatório para acesso")
        @Schema(description = "IDs dos EPIs obrigatórios para acesso à área", example = "[1, 2]")
        List<Integer> episObrigatoriosIds,

        @Schema(description = "Justificativa da alteração para histórico de auditoria", example = "Revisão periódica do mapa de riscos")
        String justificativa
) {
    public AtualizarAreaRiscoInputDTO toInputDTO() {
        return new AtualizarAreaRiscoInputDTO(
                nome,
                descricao,
                nivelPerigo,
                setorId,
                agentesRisco,
                limites,
                episObrigatoriosIds,
                justificativa
        );
    }
}
