/*
 * Resposta HTTP de uma entrada do histórico de alterações de área de risco ou tarefa (issue #114),
 * devolvida por GET /api/areas-risco/{id}/historico e GET /api/tarefas/{id}/historico.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Entrada do histórico de alterações, lida da trilha de auditoria")
public record RegistroHistoricoResponse(
        @Schema(description = "Identificador do registro de auditoria", example = "12") Integer id,

        @Schema(description = "Tipo da operação registrada", example = "ALTERACAO") String tipoOperacao,

        @Schema(description = "Data e hora da operação", example = "2026-10-02T14:30:00") LocalDateTime dataHora,

        @Schema(description = "E-mail de quem realizou a operação", example = "gestor@safeplace.com") String responsavel,

        @Schema(description = "O que mudou, com valor anterior e novo, e a justificativa informada",
                example = "Alteração da área de risco: limites: de \"Entre as linhas A e B\" para \"Entre as linhas A e C\". Justificativa: Revisão anual")
        String descricao) {

    public static RegistroHistoricoResponse fromOutputDTO(RegistroHistoricoOutputDTO dto) {
        return new RegistroHistoricoResponse(
                dto.id(), dto.tipoOperacao(), dto.dataHora(), dto.responsavel(), dto.descricao());
    }
}
