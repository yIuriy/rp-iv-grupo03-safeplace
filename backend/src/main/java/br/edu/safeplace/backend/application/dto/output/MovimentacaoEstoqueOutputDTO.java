package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueOutputDTO(
        Integer id,
        Integer epiId,
        Integer loteId,
        Integer responsavelId,
        TipoMovimentacao tipo,
        int quantidade,
        LocalDateTime dataHora,
        String motivo,
        int saldoAposMovimentacao
) {
    public MovimentacaoEstoqueOutputDTO(Integer id, Integer epiId, TipoMovimentacao tipo,
                                        int quantidade, LocalDateTime dataHora, String motivo,
                                        int saldoAposMovimentacao) {
        this(id, epiId, null, null, tipo, quantidade, dataHora, motivo, saldoAposMovimentacao);
    }
    public static MovimentacaoEstoqueOutputDTO deDominio(MovimentacaoEstoque mov, int saldoApos) {
        return new MovimentacaoEstoqueOutputDTO(
                mov.getId(),
                mov.getEpiId(),
                mov.getLoteId(),
                mov.getResponsavelId(),
                mov.getTipo(),
                mov.getQuantidade(),
                mov.getDataHora(),
                mov.getMotivo(),
                saldoApos
        );
    }
}
