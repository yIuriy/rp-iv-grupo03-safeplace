package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueOutputDTO(
        Integer id,
        Integer epiId,
        TipoMovimentacao tipo,
        int quantidade,
        LocalDateTime dataHora,
        String motivo,
        int saldoAposMovimentacao
) {
    public static MovimentacaoEstoqueOutputDTO deDominio(MovimentacaoEstoque mov, int saldoApos) {
        return new MovimentacaoEstoqueOutputDTO(
                mov.getId(),
                mov.getEpiId(),
                mov.getTipo(),
                mov.getQuantidade(),
                mov.getDataHora(),
                mov.getMotivo(),
                saldoApos
        );
    }
}
