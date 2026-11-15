package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;

import java.util.List;
import java.util.Optional;

public interface EpiRepository {

    Epi salvar(Epi epi);

    Optional<Epi> buscarPorId(Integer id);

    default Optional<Epi> buscarPorIdComLock(Integer id) {
        return buscarPorId(id);
    }

    Optional<Epi> buscarPorCA(String numeroCa);

    default List<Epi> listarPorCA(String numeroCa) {
        return buscarPorCA(numeroCa).map(List::of).orElseGet(List::of);
    }

    /** Modelo já cadastrado para o CA, fonte única do CA (decisão #124). */
    default Optional<ModeloEPI> buscarModeloPorCa(int ca) {
        return Optional.empty();
    }

    List<Epi> listar();

    Epi atualizarSaldo(Epi epi);

    MovimentacaoEstoque salvar(
            MovimentacaoEstoque movimentacao
    );

    List<MovimentacaoEstoque> listarMovimentacoes(
            Integer epiId
    );
}