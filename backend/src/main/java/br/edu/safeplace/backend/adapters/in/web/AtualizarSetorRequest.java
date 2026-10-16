package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.AtualizarSetorInputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para atualização cadastral de um setor físico (RF05)")
public record AtualizarSetorRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome do setor", example = "Galpão Principal")
        String nome,

        @Schema(description = "Descrição das atividades do setor", example = "Montagem de estruturas metálicas")
        String descricao,

        @Schema(description = "Localização física do setor na planta", example = "Bloco B, Nível 1")
        String localizacao,

        @NotNull(message = "Status/Grau de perigo é obrigatório")
        @Schema(description = "Grau de perigo do setor", example = "ALTO")
        NivelPerigo status
) {
    public AtualizarSetorInputDTO toInputDTO() {
        return new AtualizarSetorInputDTO(nome, descricao, localizacao, status);
    }
}
