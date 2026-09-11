package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;

import java.util.List;
import java.util.Optional;

public interface EpiRepositoryPort {

    Epi salvar(Epi epi);

    Optional<Epi> buscarPorId(Integer id);

    Optional<Epi> buscarPorCA(String numeroCa);

    List<Epi> listarTodos();

    Epi atualizarSaldo(Epi epi);

    MovimentacaoEstoque salvarMovimentacao(
            MovimentacaoEstoque movimentacao
    );

    List<MovimentacaoEstoque> listarMovimentacoesPorEpi(
            Integer epiId
    );

    default List<Epi> listar() {
        return listarTodos();
    }
}