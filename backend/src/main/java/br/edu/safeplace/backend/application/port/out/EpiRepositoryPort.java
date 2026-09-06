package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;

import java.util.List;
import java.util.Optional;

public interface EpiRepositoryPort {
    Epi salvar(Epi epi);
    Optional<Epi> buscarPorId(Integer id);
    List<Epi> listar();
    MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao);
    List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId);
}
