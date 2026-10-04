package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para associar EPIs obrigatórios a uma tarefa (RF12 / UC08 / US12)")
public record AssociarEpisTarefaRequest(
        @NotNull(message = "A lista de identificadores de EPIs não pode ser nula.")
        @Schema(description = "Lista de identificadores dos EPIs obrigatórios", example = "[1, 2]")
        List<Integer> epiIds) {
}
