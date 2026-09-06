package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;

import java.util.List;

public interface GerenciarEpiUseCase {
    Epi cadastrarEpi(Epi epi);
    List<Epi> listar();
    Epi buscarPorId(Integer id);
    MovimentacaoEstoque registrarMovimentacao(Integer epiId, TipoMovimentacao tipo, int quantidade, String motivo);
}
