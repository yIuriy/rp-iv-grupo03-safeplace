package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.LoteEPI;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import br.edu.safeplace.backend.application.port.in.GerenciarEstoqueUseCase;

import java.util.List;

@Service
public class EpiUseCase implements GerenciarEpiUseCase, GerenciarEstoqueUseCase {
    private final EpiRepositoryPort repositoryPort;
    private final UsuarioRepositorioPorta usuarioRepositorio;

    public EpiUseCase(EpiRepositoryPort repositoryPort) {
        this(repositoryPort, null);
    }

    @Autowired
    public EpiUseCase(EpiRepositoryPort repositoryPort, UsuarioRepositorioPorta usuarioRepositorio) {
        this.repositoryPort = repositoryPort;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional
    public EpiOutputDTO cadastrarEpi(CadastrarEpiInputDTO inputDTO) {
        ModeloEPI modelo = ModeloEPI.deCadastroLegado(inputDTO.numeroCa(), inputDTO.dataValidadeCa());
        LoteEPI lote = new LoteEPI(null, inputDTO.numeroLote(), inputDTO.notaFiscal(),
                inputDTO.dataFabricacao(), inputDTO.validadeLote(), inputDTO.quantidade(), modelo);
        Epi novoEpi = Epi.novo(
                inputDTO.nome(),
                lote,
                inputDTO.quantidade(),
                inputDTO.estoqueMinimo(),
                inputDTO.vidaUtilDias(),
                inputDTO.descricao(),
                inputDTO.classificacao(), inputDTO.localizacao());
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
    public MovimentacaoEstoqueOutputDTO registrarMovimentacao(Integer epiId, TipoMovimentacao tipo, int quantidade,
            String motivo) {
        return registrarMovimentacao(epiId, tipo, quantidade, motivo, null);
    }

    @Override
    @Transactional
    public MovimentacaoEstoqueOutputDTO registrarMovimentacao(Integer epiId, TipoMovimentacao tipo, int quantidade,
            String motivo, String responsavelEmail) {
        Epi epi = repositoryPort.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));
        Integer responsavelId = buscarResponsavelId(responsavelEmail);

        MovimentacaoEstoque movimentacao;
        if (tipo == TipoMovimentacao.ENTRADA) {
            movimentacao = epi.adicionarEstoque(quantidade, motivo, responsavelId);
        } else if (tipo == TipoMovimentacao.SAIDA) {
            movimentacao = epi.removerEstoque(quantidade, motivo, responsavelId);
        } else {
            throw new IllegalArgumentException("Tipo de movimentação inválido: " + tipo);
        }

        repositoryPort.atualizarSaldo(epi);
        MovimentacaoEstoque salvo = repositoryPort.salvarMovimentacao(movimentacao);
        return MovimentacaoEstoqueOutputDTO.deDominio(salvo, epi.getQuantidade());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueOutputDTO> buscarHistorico(Integer epiId) {
        Epi epi = repositoryPort.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));
        return repositoryPort.listarMovimentacoesPorEpi(epiId).stream()
                .map(movimentacao -> MovimentacaoEstoqueOutputDTO.deDominio(movimentacao, epi.getQuantidade()))
                .toList();
    }

    private Integer buscarResponsavelId(String responsavelEmail) {
        if (responsavelEmail == null || responsavelEmail.isBlank() || usuarioRepositorio == null) {
            return null;
        }
        return usuarioRepositorio.buscarPorEmail(responsavelEmail.toLowerCase())
                .map(usuario -> usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Responsável autenticado não encontrado."));
    }

    @Override
    @Transactional
    public MovimentacaoEstoqueOutputDTO registrarEntrada(
            Integer epiId,
            int quantidade,
            String motivo) {
        return registrarMovimentacao(
                epiId,
                TipoMovimentacao.ENTRADA,
                quantidade,
                motivo);
    }

    @Override
    @Transactional
    public MovimentacaoEstoqueOutputDTO darBaixaEstoque(
            Integer epiId,
            int quantidade,
            String motivo) {
        return registrarMovimentacao(
                epiId,
                TipoMovimentacao.SAIDA,
                quantidade,
                motivo);
    }

    @Override
    @Transactional(readOnly = true)
    public int consultarSaldoDisponivel(Integer epiId) {
        return repositoryPort.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId))
                .getQuantidade();
    }
}
