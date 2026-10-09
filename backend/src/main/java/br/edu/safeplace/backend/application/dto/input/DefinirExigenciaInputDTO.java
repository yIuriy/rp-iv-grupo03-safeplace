package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao.TipoAlvo;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao.TipoCapacitacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Exigência de certificação ou treinamento para uma tarefa ou EPI (UC02)")
public record DefinirExigenciaInputDTO(
        @NotBlank(message = "Nome da capacitação exigida é obrigatório.")
        @Schema(example = "NR-35 Trabalho em Altura") String nomeCapacitacao,
        @NotNull(message = "Tipo da capacitação é obrigatório.") TipoCapacitacao tipoCapacitacao,
        @NotNull(message = "Alvo da exigência é obrigatório.") TipoAlvo alvo,
        @NotNull(message = "Identificador do alvo é obrigatório.")
        @Schema(example = "5") Integer alvoId
) {
}
