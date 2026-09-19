package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Dados para registrar uma manutenção concluída de EPI")
public record RegistrarManutencaoRequest(
        @NotNull(message = "Data da manutenção é obrigatória")
        @Schema(example = "2026-09-19T14:30:00")
        LocalDateTime dataManutencao,

        @NotBlank(message = "Descrição da manutenção é obrigatória")
        @Schema(example = "Higienização e inspeção visual concluídas")
        String descricao,

        @NotNull(message = "Resultado da manutenção é obrigatório")
        @Schema(example = "APROVADO")
        ResultadoManutencao resultado,

        @Schema(description = "Opcional; não é exigido pelo MVP", example = "PREVENTIVA")
        TipoManutencao tipoManutencao
) {
    public ConcluirManutencaoInputDTO toInputDTO(Integer epiId) {
        return new ConcluirManutencaoInputDTO(
                epiId, dataManutencao, tipoManutencao, descricao, resultado, null);
    }
}
