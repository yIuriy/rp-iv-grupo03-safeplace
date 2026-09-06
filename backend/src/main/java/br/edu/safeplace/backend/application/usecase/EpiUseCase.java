package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
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
public class EpiUseCase implements GerenciarEpiUseCase {
    private final EpiRepositoryPort repositoryPort;

    public EpiUseCase(EpiRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public EpiOutputDTO cadastrarEpi(CadastrarEpiInputDTO inputDTO) {
        Epi novoEpi = Epi.novo(
                inputDTO.nome(),
                inputDTO.numeroCa(),
                inputDTO.quantidade(),
                inputDTO.estoqueMinimo(),
                inputDTO.dataValidadeCa(),
                inputDTO.vidaUtilDias()
        );
        Epi salvo = repositoryPort.salvar(novoEpi);
        return EpiOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpiOutputDTO> listar() {
        return repositoryPort.listar().stream()
                .map(EpiOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EpiOutputDTO buscarPorId(Integer id) {
        Epi epi = repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EpiNaoEncontradoException(id));
        return EpiOutputDTO.deDominio(epi);
    }

    @Override
    @Transactional
    public MovimentacaoEstoqueOutputDTO registrarMovimentacao(Integer epiId, TipoMovimentacao tipo, int quantidade, String motivo) {
        Epi epi = repositoryPort.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));

        MovimentacaoEstoque movimentacao;
        if (tipo == TipoMovimentacao.ENTRADA) {
            movimentacao = epi.adicionarEstoque(quantidade, motivo);
        } else if (tipo == TipoMovimentacao.SAIDA) {
            movimentacao = epi.removerEstoque(quantidade, motivo);
        } else {
            throw new IllegalArgumentException("Tipo de movimentação inválido: " + tipo);
        }

        repositoryPort.salvar(epi);
        MovimentacaoEstoque salvo = repositoryPort.salvarMovimentacao(movimentacao);
        return MovimentacaoEstoqueOutputDTO.deDominio(salvo, epi.getQuantidade());
    }
}
