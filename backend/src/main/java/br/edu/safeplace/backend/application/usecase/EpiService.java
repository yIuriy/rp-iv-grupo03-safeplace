package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.epi.EpiValidador;
import br.edu.safeplace.backend.validacao.epi.ModeloEpiValidador;
import br.edu.safeplace.backend.validacao.epi.MovimentacaoEstoqueValidador;
import br.edu.safeplace.backend.validacao.epi.NumeroCaValidador;

@Service
public class EpiService implements GerenciarEpiUseCase, GerenciarEstoqueUseCase {
    private final Validador<String> numeroCaValidador;
    private final Validador<ModeloEPI> modeloValidador;
    private final Validador<Epi> epiValidador;
    private final Validador<MovimentacaoEstoque> movimentacaoValidador;
    private final EpiRepository repository;
    private final UsuarioRepository usuarioRepositorio;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final OperadorAtual operadorAtual;

    @Autowired
    public EpiService(
            EpiRepository repository,
            UsuarioRepository usuarioRepositorio,
            LogAuditoriaRepository logAuditoriaRepository,
            @Qualifier("numeroCaValidador") Validador<String> numeroCaValidador,
            Validador<ModeloEPI> modeloValidador,
            Validador<Epi> epiValidador,
            Validador<MovimentacaoEstoque> movimentacaoValidador,
            OperadorAtual operadorAtual) {
        this.repository = repository;
        this.usuarioRepositorio = usuarioRepositorio;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        this.numeroCaValidador = numeroCaValidador;
        this.modeloValidador = modeloValidador;
        this.epiValidador = epiValidador;
        this.movimentacaoValidador = movimentacaoValidador;
        // Adapter (#236): o operador vem da porta OperadorAutenticado.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
    }

    /** Monta os validadores padrão; usado pelos testes sem contexto Spring. */
    public EpiService(
            EpiRepository repository,
            UsuarioRepository usuarioRepositorio,
            LogAuditoriaRepository logAuditoriaRepository,
            OperadorAtual operadorAtual) {
        this(repository, usuarioRepositorio, logAuditoriaRepository, new NumeroCaValidador(),
                new ModeloEpiValidador(), new EpiValidador(), new MovimentacaoEstoqueValidador(), operadorAtual);
    }

    @Override
    @Transactional
    public EpiOutputDTO cadastrarEpi(CadastrarEpiInputDTO inputDTO) {
        // Chain of Responsibility (#264): CA e modelo antes da busca pelo modelo existente.
        numeroCaValidador.validar(inputDTO.numeroCa());
        ModeloEPI informado = ModeloEPI.deCadastroLegado(
                inputDTO.numeroCa(), inputDTO.marca(), inputDTO.dataValidadeCa());
        modeloValidador.validar(informado);
        // Decisão #124: o CA tem fonte única; um CA já cadastrado reaproveita o mesmo modelo.
        ModeloEPI modelo = repository.buscarModeloPorCa(informado.getCa())
                .map(existente -> existente.reutilizarPara(informado))
                .orElse(informado);
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
        epiValidador.validar(novoEpi);
        Epi salvo = repository.salvar(novoEpi);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                operadorAtual.emailOuSistema(),
                "Epi",
                salvo.getId(),
                "Cadastro de EPI: " + salvo.getNome()
        );
        logAuditoriaRepository.salvar(log);

        return EpiOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpiOutputDTO> listar() {
        return repository.listar().stream()
                .map(EpiOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EpiOutputDTO buscarPorId(Integer id) {
        Epi epi = repository.buscarPorId(id)
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
        Epi epi = repository.buscarPorIdComLock(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));
        // O e-mail vindo do controller tem precedência; sem ele, vale o operador autenticado (#236).
        Integer responsavelId = buscarResponsavelId(
                operadorAtual.emailInformadoOuAutenticado(responsavelEmail).orElse(null));

        MovimentacaoEstoque movimentacao;
        if (tipo == TipoMovimentacao.ENTRADA) {
            movimentacao = epi.adicionarEstoque(quantidade, motivo, responsavelId);
        } else if (tipo == TipoMovimentacao.SAIDA) {
            movimentacao = epi.removerEstoque(quantidade, motivo, responsavelId);
        } else {
            throw new IllegalArgumentException("Tipo de movimentação inválido: " + tipo);
        }
        movimentacaoValidador.validar(movimentacao);

        repository.atualizarSaldo(epi);
        MovimentacaoEstoque salvo = repository.salvarMovimentacao(movimentacao);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                operadorAtual.emailInformadoOuSistema(responsavelEmail),
                "Epi",
                epi.getId(),
                "Movimentação de estoque: " + tipo + " (" + quantidade + " un). Motivo: " + motivo
        );
        logAuditoriaRepository.salvar(log);

        return MovimentacaoEstoqueOutputDTO.deDominio(salvo, epi.getQuantidade());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueOutputDTO> buscarHistorico(Integer epiId) {
        Epi epi = repository.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));
        int saldoAposMovimentacao = epi.getQuantidade();
        List<MovimentacaoEstoqueOutputDTO> historico = new ArrayList<>();
        List<MovimentacaoEstoque> movimentacoes = repository.listarMovimentacoesPorEpi(epiId).stream()
                .sorted(Comparator.comparing(MovimentacaoEstoque::getDataHora).reversed())
                .toList();
        for (MovimentacaoEstoque movimentacao : movimentacoes) {
            historico.add(MovimentacaoEstoqueOutputDTO.deDominio(movimentacao, saldoAposMovimentacao));
            saldoAposMovimentacao += movimentacao.getTipo() == TipoMovimentacao.ENTRADA
                    ? -movimentacao.getQuantidade()
                    : movimentacao.getQuantidade();
        }
        return historico;
    }

    /** US03: o responsável pela movimentação é o usuário autenticado e é obrigatório. */
    private Integer buscarResponsavelId(String responsavelEmail) {
        if (responsavelEmail == null || responsavelEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Responsável pela movimentação é obrigatório: nenhum usuário autenticado.");
        }
        if (usuarioRepositorio == null) {
            throw new IllegalStateException("Consulta de usuários indisponível para identificar o responsável.");
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
        return repository.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId))
                .getQuantidade();
    }
}
