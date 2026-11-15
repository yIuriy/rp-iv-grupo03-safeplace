package br.edu.safeplace.backend.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AlocarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.AlocacaoTarefaRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.aptidao.AptidaoParaTarefa;
import br.edu.safeplace.backend.validacao.tarefa.AlocacaoTarefaValidador;

@Service
public class AlocarColaboradorTarefaService implements AlocarColaboradorTarefaUseCase {

    private static final String ENTIDADE_AUDITADA = "AlocacaoTarefa";

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CapacitacaoRepository capacitacaoRepository;
    private final AlocacaoTarefaRepository alocacaoTarefaRepository;
    private final RegistradorAuditoria auditoria;
    private final Validador<AptidaoParaTarefa> validadorAptidao;
    private final Validador<AlocacaoTarefa> validadorAlocacao;
    private final Clock clock;

    /** Usa o validador padrão da alocação; usado pelos testes sem contexto Spring. */
    public AlocarColaboradorTarefaService(
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository,
            CapacitacaoRepository capacitacaoRepository,
            AlocacaoTarefaRepository alocacaoTarefaRepository,
            LogAuditoriaRepository auditoriaRepository,
            Validador<AptidaoParaTarefa> validadorAptidao) {
        this(tarefaRepository, usuarioRepository, capacitacaoRepository, alocacaoTarefaRepository,
                new RegistradorAuditoria(auditoriaRepository), validadorAptidao, new AlocacaoTarefaValidador(), Clock.systemDefaultZone());
    }

    public AlocarColaboradorTarefaService(
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository,
            CapacitacaoRepository capacitacaoRepository,
            AlocacaoTarefaRepository alocacaoTarefaRepository,
            LogAuditoriaRepository auditoriaRepository,
            Validador<AptidaoParaTarefa> validadorAptidao,
            Validador<AlocacaoTarefa> validadorAlocacao) {
        this(tarefaRepository, usuarioRepository, capacitacaoRepository, alocacaoTarefaRepository,
                new RegistradorAuditoria(auditoriaRepository), validadorAptidao, validadorAlocacao, Clock.systemDefaultZone());
    }

    @Autowired
    public AlocarColaboradorTarefaService(
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository,
            CapacitacaoRepository capacitacaoRepository,
            AlocacaoTarefaRepository alocacaoTarefaRepository,
            RegistradorAuditoria auditoria,
            Validador<AptidaoParaTarefa> validadorAptidao,
            Validador<AlocacaoTarefa> validadorAlocacao,
            Clock clock) {
        this.validadorAlocacao = validadorAlocacao;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.capacitacaoRepository = capacitacaoRepository;
        this.alocacaoTarefaRepository = alocacaoTarefaRepository;
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        this.validadorAptidao = validadorAptidao;
        this.clock = java.util.Objects.requireNonNull(clock, "Clock é obrigatório.");
    }

    @Override
    @Transactional
    public AlocacaoTarefaOutputDTO alocar(Integer tarefaId, AlocarColaboradorInputDTO entrada, String operadorEmail) {
        Colaborador operador = resolverOperador(operadorEmail);
        Tarefa tarefa = tarefaRepository.buscarPorId(tarefaId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));
        Colaborador colaborador = usuarioRepository.buscarPorId(entrada.colaboradorId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Colaborador não encontrado: " + entrada.colaboradorId()));

        List<Certificacao> certificacoes = capacitacaoRepository.listarCertificacoes(colaborador.getId());
        List<Treinamento> treinamentos = capacitacaoRepository.listarTreinamentos(colaborador.getId());
        List<ExigenciaCapacitacao> exigencias = capacitacaoRepository.listarExigencias(
                ExigenciaCapacitacao.TipoAlvo.TAREFA, tarefa.getId());

        // Chain of Responsibility (#228): valida antes do repository; se falhar, nada é gravado.
        validadorAptidao.validar(new AptidaoParaTarefa(
                colaborador, tarefa, certificacoes, treinamentos, exigencias, LocalDate.now(clock)));

        AlocacaoTarefa alocacao = AlocacaoTarefa.nova(tarefa, colaborador, operador, entrada.observacao());
        validadorAlocacao.validar(alocacao);
        AlocacaoTarefa salva = alocacaoTarefaRepository.salvar(alocacao);

        auditoria.por(operadorEmail).cadastro(
                ENTIDADE_AUDITADA,
                salva.getId(),
                "Alocação do colaborador " + colaborador.getNome() + " (ID " + colaborador.getId()
                        + ") na tarefa " + tarefa.getDescricao() + " (ID " + tarefa.getId() + ")"
        );

        return AlocacaoTarefaOutputDTO.deDominio(salva, tarefa.getDescricao(), colaborador.getNome(), operador.getNome());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaAptidaoOutputDTO consultarAptidao(Integer tarefaId, Integer colaboradorId) {
        Tarefa tarefa = tarefaRepository.buscarPorId(tarefaId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(tarefaId));
        Colaborador colaborador = usuarioRepository.buscarPorId(colaboradorId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Colaborador não encontrado: " + colaboradorId));

        List<Certificacao> certificacoes = capacitacaoRepository.listarCertificacoes(colaborador.getId());
        List<Treinamento> treinamentos = capacitacaoRepository.listarTreinamentos(colaborador.getId());
        List<ExigenciaCapacitacao> exigencias = capacitacaoRepository.listarExigencias(
                ExigenciaCapacitacao.TipoAlvo.TAREFA, tarefa.getId());

        // Mesma cadeia da alocação: a consulta e o bloqueio não podem discordar.
        List<String> impedimentos = validadorAptidao.verificar(new AptidaoParaTarefa(
                colaborador, tarefa, certificacoes, treinamentos, exigencias, LocalDate.now(clock))).mensagens();

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
