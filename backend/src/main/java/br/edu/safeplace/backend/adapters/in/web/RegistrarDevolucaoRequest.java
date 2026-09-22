/*
 * Corpo de POST /api/emprestimos/{id}/devolucao: estado do item devolvido (UC12, alternativo I).
 */
package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para registrar a devolução de um EPI emprestado")
public record RegistrarDevolucaoRequest(
        @NotNull(message = "Estado na devolução é obrigatório (APTO, MANUTENCAO ou DESCARTE)")
        @Schema(description = "Estado do item devolvido", example = "APTO")
        EstadoDevolucao estado) {

    public RegistrarDevolucaoInputDTO toInputDTO() {
        return new RegistrarDevolucaoInputDTO(estado);
    }
}
