package br.edu.safeplace.backend.application.dto.capacitacao;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Colaborador com pendência de capacitação (UC02, cenário alternativo III)")
public record PendenciaColaboradorOutputDTO(
        @Schema(description = "Identificador do colaborador", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador") String colaboradorNome,
        @Schema(description = "Mensagens da pendência") List<String> mensagens
) {
}
