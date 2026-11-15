package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;

public interface GerenciarEstoqueUseCase {
    MovimentacaoEstoqueOutputDTO registrarEntrada(
            Integer epiId,
            int quantidade,
            String motivo);

    MovimentacaoEstoqueOutputDTO darBaixa(
            Integer epiId,
            int quantidade,
            String motivo);

    int consultarSaldo(Integer epiId);
}
