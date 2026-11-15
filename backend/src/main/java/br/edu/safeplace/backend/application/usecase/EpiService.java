package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.EspecificacaoEPI;
import br.edu.safeplace.backend.domain.epi.FabricaEpi;
import br.edu.safeplace.backend.domain.epi.FabricaEpiPadrao;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
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
    private final RegistradorAuditoria auditoria;
    private final OperadorAtual operadorAtual;
    private final FabricaEpi fabricaEpi;

    @Autowired
    public EpiService(
            EpiRepository repository,
            UsuarioRepository usuarioRepositorio,
            RegistradorAuditoria auditoria,
            @Qualifier("numeroCaValidador") Validador<String> numeroCaValidador,
            Validador<ModeloEPI> modeloValidador,
            Validador<Epi> epiValidador,
            Validador<MovimentacaoEstoque> movimentacaoValidador,
            OperadorAtual operadorAtual,
            FabricaEpi fabricaEpi) {
        this.repository = repository;
        this.usuarioRepositorio = usuarioRepositorio;
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        this.numeroCaValidador = numeroCaValidador;
        this.modeloValidador = modeloValidador;
        this.epiValidador = epiValidador;
        this.movimentacaoValidador = movimentacaoValidador;
        // Adapter (#236): o operador vem da porta OperadorAutenticado.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
        // Factory Method (#225): o EPI novo é criado pela fábrica injetada, por interface.
        this.fabricaEpi = java.util.Objects.requireNonNull(fabricaEpi, "Fábrica de EPI é obrigatória.");
    }

    public EpiService(
            EpiRepository repository,
            UsuarioRepository usuarioRepositorio,
            LogAuditoriaRepository logAuditoriaRepository,
            @Qualifier("numeroCaValidador") Validador<String> numeroCaValidador,
            Validador<ModeloEPI> modeloValidador,
            Validador<Epi> epiValidador,
            Validador<MovimentacaoEstoque> movimentacaoValidador,
            OperadorAtual operadorAtual,
            FabricaEpi fabricaEpi) {
        this(repository, usuarioRepositorio, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                numeroCaValidador, modeloValidador, epiValidador, movimentacaoValidador, operadorAtual, fabricaEpi);
    }

    /** Monta os validadores padrão; usado pelos testes sem contexto Spring. */
    public EpiService(
            EpiRepository repository,
            UsuarioRepository usuarioRepositorio,
            LogAuditoriaRepository logAuditoriaRepository,
            OperadorAtual operadorAtual) {
        this(repository, usuarioRepositorio, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                new NumeroCaValidador(), new ModeloEpiValidador(), new EpiValidador(),
                new MovimentacaoEstoqueValidador(), operadorAtual, new FabricaEpiPadrao());
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
        EspecificacaoEPI especificacao = new EspecificacaoEPI(
                inputDTO.descricao(), inputDTO.estoqueMinimo(), inputDTO.classificacao());
        Epi novoEpi = fabricaEpi.criar(inputDTO.nome(), especificacao, lote, inputDTO.quantidade(),
                inputDTO.vidaUtilDias(), inputDTO.localizacao());
        epiValidador.validar(novoEpi);
        Epi salvo = repository.salvar(novoEpi);

        auditoria.cadastro(
                "Epi",
                salvo.getId(),
                "Cadastro de EPI: " + salvo.getNome()
        );

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
        Colaborador responsavel = buscarResponsavel(
                operadorAtual.emailInformadoOuAutenticado(responsavelEmail).orElse(null));

        MovimentacaoEstoque movimentacao;
        if (tipo == TipoMovimentacao.ENTRADA) {
            movimentacao = epi.adicionarEstoque(quantidade, motivo, responsavel);
        } else if (tipo == TipoMovimentacao.SAIDA) {
            movimentacao = epi.removerEstoque(quantidade, motivo, responsavel);
        } else {
            throw new IllegalArgumentException("Tipo de movimentação inválido: " + tipo);
        }
        movimentacaoValidador.validar(movimentacao);

        repository.atualizarSaldo(epi);
        MovimentacaoEstoque salvo = repository.salvar(movimentacao);

        auditoria.por(responsavelEmail).alteracao(
                "Epi",
                epi.getId(),
                "Movimentação de estoque: " + tipo + " (" + quantidade + " un). Motivo: " + motivo
        );

        return MovimentacaoEstoqueOutputDTO.deDominio(salvo, epi.getQuantidade());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueOutputDTO> buscarHistorico(Integer epiId) {
        Epi epi = repository.buscarPorId(epiId)
                .orElseThrow(() -> new EpiNaoEncontradoException(epiId));
        int saldoAposMovimentacao = epi.getQuantidade();
        List<MovimentacaoEstoqueOutputDTO> historico = new ArrayList<>();
        List<MovimentacaoEstoque> movimentacoes = repository.listarMovimentacoes(epiId).stream()
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
    private Colaborador buscarResponsavel(String responsavelEmail) {
        if (responsavelEmail == null || responsavelEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Responsável pela movimentação é obrigatório: nenhum usuário autenticado.");
        }
        if (usuarioRepositorio == null) {
            throw new IllegalStateException("Consulta de usuários indisponível para identificar o responsável.");
        }
        return usuarioRepositorio.buscarPorEmail(responsavelEmail.toLowerCase())
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
