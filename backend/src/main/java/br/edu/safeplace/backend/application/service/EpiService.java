package br.edu.safeplace.backend.application.service;

import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EpiService implements GerenciarEpiUseCase {
    private final EpiRepositoryPort repositoryPort;

    public EpiService(EpiRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public Epi cadastrarEpi(Epi epi) {
        return repositoryPort.salvar(epi);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Epi> listar() {
        return repositoryPort.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public Epi buscarPorId(Integer id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EpiNaoEncontradoException(id));
    }

    @Override
    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(Integer epiId, TipoMovimentacao tipo, int quantidade, String motivo) {
        Epi epi = buscarPorId(epiId);

        MovimentacaoEstoque movimentacao;
        if (tipo == TipoMovimentacao.ENTRADA) {
            movimentacao = epi.adicionarEstoque(quantidade, motivo);
        } else if (tipo == TipoMovimentacao.SAIDA) {
            movimentacao = epi.removerEstoque(quantidade, motivo);
        } else {
            throw new IllegalArgumentException("Tipo de movimentação inválido: " + tipo);
        }

        repositoryPort.salvar(epi);
        return repositoryPort.salvarMovimentacao(movimentacao);
    }
}
