/*
 * Corpo de POST /api/emprestimos: entrega de um EPI a um colaborador cadastrado (UC12). O operador
 * é o usuário autenticado e não é aceito no corpo.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;

import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para registrar a entrega de um EPI a um colaborador")
public record RegistrarEntregaRequest(
        @NotNull(message = "EPI é obrigatório")
        @Schema(description = "Código do EPI entregue", example = "1")
        Integer epiId,

        @NotNull(message = "Colaborador é obrigatório")
        @Schema(description = "Identificador do colaborador que recebe o EPI", example = "3")
        Integer colaboradorId,

        @NotNull(message = "Data prevista de devolução é obrigatória")
        @Schema(description = "Prazo combinado para a devolução", example = "2026-10-21")
        LocalDate dataDevolucaoPrevista,

        @Schema(description = "Confirma a substituição quando o colaborador já possui EPI do mesmo tipo em posse", example = "false")
        Boolean confirmarSubstituicao,

        @Schema(description = "Justificativa da substituição, obrigatória quando há duplicidade confirmada")
        String justificativaSubstituicao) {

    public RegistrarEntregaInputDTO toInputDTO() {
        return new RegistrarEntregaInputDTO(epiId, colaboradorId, dataDevolucaoPrevista,
                Boolean.TRUE.equals(confirmarSubstituicao), justificativaSubstituicao);
    }
}
