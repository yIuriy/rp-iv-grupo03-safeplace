/*
 * Resposta HTTP de uma ação do histórico do próprio perfil, devolvida por
 * GET /api/usuarios/me/historico (RNF05).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.output.AcaoUsuarioOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ação registrada pela pessoa autenticada, lida da trilha de auditoria")
public record AcaoUsuarioResponse(
        @Schema(description = "Identificador do registro de auditoria", example = "42") Integer id,

        @Schema(description = "Tipo da operação: CADASTRO, ALTERACAO ou REMOCAO", example = "CADASTRO")
        String tipoOperacao,

        @Schema(description = "Data e hora da operação", example = "2026-10-05T20:53:27") LocalDateTime dataHora,

        @Schema(description = "Tipo do registro afetado, como gravado no log", example = "Incidente") String entidade,

        @Schema(description = "Identificador do registro afetado", example = "4") Integer entidadeId,

        @Schema(description = "Justificativa ou descrição gravada com a operação, quando houver",
                example = "Registro de incidente realizado") String descricao) {

    public static AcaoUsuarioResponse fromOutputDTO(AcaoUsuarioOutputDTO dto) {
        return new AcaoUsuarioResponse(
                dto.id(), dto.tipoOperacao(), dto.dataHora(), dto.entidade(), dto.entidadeId(), dto.descricao());
    }
}
