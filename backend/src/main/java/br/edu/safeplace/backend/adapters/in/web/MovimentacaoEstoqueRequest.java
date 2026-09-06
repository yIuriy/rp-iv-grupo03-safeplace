package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Requisição para registrar movimentação de estoque de EPI")
public record MovimentacaoEstoqueRequest(
        @NotNull(message = "Tipo de movimentação é obrigatório (ENTRADA ou SAIDA)")
        @Schema(description = "Tipo de movimentação: ENTRADA ou SAIDA", example = "ENTRADA")
        TipoMovimentacao tipo,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade movimentada deve ser maior que zero")
        @Schema(description = "Quantidade de unidades a movimentar", example = "10")
        Integer quantidade,

        @Schema(description = "Motivo ou justificativa técnica da movimentação", example = "Recebimento de novo lote do fornecedor")
        String motivo
) {
}
