package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AlocarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.AlocacaoTarefaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.capacitacao.ValidadorAptidaoColaboradorService;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

@Service
public class AlocarColaboradorTarefaUseCaseImpl implements AlocarColaboradorTarefaUseCase {

    private static final String ENTIDADE_AUDITADA = "AlocacaoTarefa";

    private final TarefaRepositoryPort tarefaRepository;
    private final UsuarioRepositorioPorta usuarioRepository;
    private final CapacitacaoRepositoryPort capacitacaoRepository;
    private final AlocacaoTarefaRepositoryPort alocacaoTarefaRepository;
    private final LogAuditoriaRepositoryPort auditoriaRepository;
    private final ValidadorAptidaoColaboradorService validadorAptidaoService;

    @Autowired
    public AlocarColaboradorTarefaUseCaseImpl(
            TarefaRepositoryPort tarefaRepository,
            UsuarioRepositorioPorta usuarioRepository,
            CapacitacaoRepositoryPort capacitacaoRepository,
            AlocacaoTarefaRepositoryPort alocacaoTarefaRepository,
            LogAuditoriaRepositoryPort auditoriaRepository) {
        this(tarefaRepository, usuarioRepository, capacitacaoRepository, alocacaoTarefaRepository,
                auditoriaRepository, new ValidadorAptidaoColaboradorService());
    }

    public AlocarColaboradorTarefaUseCaseImpl(
            TarefaRepositoryPort tarefaRepository,
            UsuarioRepositorioPorta usuarioRepository,
            CapacitacaoRepositoryPort capacitacaoRepository,
            AlocacaoTarefaRepositoryPort alocacaoTarefaRepository,
            LogAuditoriaRepositoryPort auditoriaRepository,
            ValidadorAptidaoColaboradorService validadorAptidaoService) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.capacitacaoRepository = capacitacaoRepository;
        this.alocacaoTarefaRepository = alocacaoTarefaRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.validadorAptidaoService = validadorAptidaoService;
    }

    @Override
    @Transactional
    public AlocacaoTarefaOutputDTO alocar(Integer tarefaId, AlocarColaboradorInputDTO entrada, String operadorEmail) {
        Colaborador operador = resolverOperador(operadorEmail);
        Tarefa tarefa = tarefaRepository.buscarPorId(tarefaId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));
        Colaborador colaborador = usuarioRepository.buscarPorId(entrada.colaboradorId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Colaborador não encontrado: " + entrada.colaboradorId()));

        List<Certificacao> certificacoes = capacitacaoRepository.buscarCertificacoesPorColaborador(colaborador.getId());
        List<Treinamento> treinamentos = capacitacaoRepository.buscarTreinamentosPorColaborador(colaborador.getId());
        List<ExigenciaCapacitacao> exigencias = capacitacaoRepository.buscarExigenciasPorAlvo(
                ExigenciaCapacitacao.TipoAlvo.TAREFA, tarefa.getId());

        LocalDate hoje = LocalDate.now();
        // Validação estrita e atômica: se falhar, lança exceção e nada é gravado
        validadorAptidaoService.validarAptidaoParaTarefa(colaborador, tarefa, certificacoes, treinamentos, exigencias, hoje);

        AlocacaoTarefa alocacao = AlocacaoTarefa.nova(tarefa.getId(), colaborador.getId(), operador.getId(), entrada.observacao());
        AlocacaoTarefa salva = alocacaoTarefaRepository.salvar(alocacao);

        auditoriaRepository.salvar(LogAuditoria.registrarCadastro(
                operadorEmail,
                ENTIDADE_AUDITADA,
                salva.getId(),
                "Alocação do colaborador " + colaborador.getNome() + " (ID " + colaborador.getId()
                        + ") na tarefa " + tarefa.getDescricao() + " (ID " + tarefa.getId() + ")"
        ));

        return AlocacaoTarefaOutputDTO.deDominio(salva, tarefa.getDescricao(), colaborador.getNome(), operador.getNome());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaAptidaoOutputDTO consultarAptidao(Integer tarefaId, Integer colaboradorId) {
        Tarefa tarefa = tarefaRepository.buscarPorId(tarefaId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));
        Colaborador colaborador = usuarioRepository.buscarPorId(colaboradorId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Colaborador não encontrado: " + colaboradorId));

        List<Certificacao> certificacoes = capacitacaoRepository.buscarCertificacoesPorColaborador(colaborador.getId());
        List<Treinamento> treinamentos = capacitacaoRepository.buscarTreinamentosPorColaborador(colaborador.getId());
        List<ExigenciaCapacitacao> exigencias = capacitacaoRepository.buscarExigenciasPorAlvo(
                ExigenciaCapacitacao.TipoAlvo.TAREFA, tarefa.getId());

        List<String> impedimentos = validadorAptidaoService.obterImpedimentos(
                colaborador, tarefa, certificacoes, treinamentos, exigencias, LocalDate.now());

        if (impedimentos.isEmpty()) {
            return ConsultaAptidaoOutputDTO.apto(tarefa.getId(), tarefa.getDescricao(), colaborador.getId(), colaborador.getNome());
        }
        return ConsultaAptidaoOutputDTO.inapto(tarefa.getId(), tarefa.getDescricao(), colaborador.getId(), colaborador.getNome(), impedimentos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlocacaoTarefaOutputDTO> listarPorTarefa(Integer tarefaId) {
        Tarefa tarefa = tarefaRepository.buscarPorId(tarefaId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));

        return alocacaoTarefaRepository.listarPorTarefa(tarefaId).stream()
                .map(aloc -> {
                    String colaboradorNome = usuarioRepository.buscarPorId(aloc.getColaboradorId())
                            .map(Colaborador::getNome).orElse("Desconhecido");
                    String responsavelNome = usuarioRepository.buscarPorId(aloc.getResponsavelAlocacaoId())
                            .map(Colaborador::getNome).orElse("Desconhecido");
                    return AlocacaoTarefaOutputDTO.deDominio(aloc, tarefa.getDescricao(), colaboradorNome, responsavelNome);
                })
                .toList();
    }

    private Colaborador resolverOperador(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail do operador autenticado é obrigatório.");
        }
        return usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Operador autenticado não encontrado para o e-mail: " + email));
    }
}
