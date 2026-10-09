package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Cadastro de certificação de um colaborador (UC02)")
public record CadastrarCertificacaoInputDTO(
        @NotBlank(message = "Nome da certificação é obrigatório.")
        @Schema(example = "NR-35 Trabalho em Altura") String nome,
        @NotNull(message = "Data de vencimento é obrigatória.")
        @Schema(example = "2027-01-31") LocalDate dataVencimento,
        @NotNull(message = "Data de emissão é obrigatória.")
        @Schema(example = "2026-01-31") LocalDate dataEmissao,
        @NotNull(message = "Identificador do colaborador é obrigatório.")
        @Schema(example = "3") Integer colaboradorId
) {
}
