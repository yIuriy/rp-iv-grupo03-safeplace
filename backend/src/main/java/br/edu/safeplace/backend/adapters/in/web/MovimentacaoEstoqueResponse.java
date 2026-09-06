package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta do registro de movimentação de estoque")
public record MovimentacaoEstoqueResponse(
        @Schema(description = "Identificador da movimentação", example = "1")
        Integer id,

        @Schema(description = "ID do EPI movimentado", example = "1")
        Integer epiId,

        @Schema(description = "Tipo de movimentação realizada", example = "ENTRADA")
        TipoMovimentacao tipo,

        @Schema(description = "Quantidade movimentada", example = "10")
        int quantidade,

        @Schema(description = "Data e hora do registro", example = "2026-09-06T16:00:00")
        LocalDateTime dataHora,

        @Schema(description = "Motivo ou justificativa", example = "Recebimento de lote")
        String motivo,

        @Schema(description = "Saldo resultante do EPI após a movimentação", example = "35")
        int saldoAtual
) {
    public static MovimentacaoEstoqueResponse fromDomain(MovimentacaoEstoque mov, int saldoAtual) {
        return new MovimentacaoEstoqueResponse(
                mov.getId(),
                mov.getEpiId(),
                mov.getTipo(),
                mov.getQuantidade(),
                mov.getDataHora(),
                mov.getMotivo(),
                saldoAtual
        );
    }

    public static MovimentacaoEstoqueResponse fromOutputDTO(br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO dto) {
        return new MovimentacaoEstoqueResponse(
                dto.id(),
                dto.epiId(),
                dto.tipo(),
                dto.quantidade(),
                dto.dataHora(),
                dto.motivo(),
                dto.saldoAposMovimentacao()
        );
    }
}
