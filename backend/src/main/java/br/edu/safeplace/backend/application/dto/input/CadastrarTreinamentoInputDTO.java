package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Cadastro de treinamento realizado por um ou mais colaboradores (UC02)")
public record CadastrarTreinamentoInputDTO(
        @NotBlank(message = "Nome do treinamento é obrigatório.")
        @Schema(example = "NR-10 Segurança em Eletricidade") String nome,
        String conteudo,
        String instrutor,
        @NotNull(message = "Data de realização é obrigatória.")
        @Schema(example = "2026-03-10") LocalDate dataRealizacao,
        @Positive(message = "Carga horária deve ser positiva.")
        @Schema(example = "40") Integer cargaHoraria,
        @Positive(message = "Validade deve ser de ao menos 1 mês.")
        @Schema(description = "Validade em meses; omitida quando o treinamento não vence", example = "12") Integer validadeMeses,
        @NotEmpty(message = "Informe ao menos um colaborador.")
        List<@NotNull Integer> colaboradoresIds
) {
}
