package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;

public interface GerenciarEstoqueUseCase {
    MovimentacaoEstoqueOutputDTO registrarEntrada(
            Integer epiId,
            int quantidade,
            String motivo);

    MovimentacaoEstoqueOutputDTO darBaixaEstoque(
            Integer epiId,
            int quantidade,
            String motivo);

    int consultarSaldoDisponivel(Integer epiId);
}
