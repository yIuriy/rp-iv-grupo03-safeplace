package br.edu.safeplace.backend.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ExigenciaCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.CadastrarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.capacitacao.CertificacaoValidador;
import br.edu.safeplace.backend.validacao.capacitacao.ExigenciaCapacitacaoValidador;
import br.edu.safeplace.backend.validacao.capacitacao.TreinamentoValidador;

@Service
@Transactional
public class CadastrarCapacitacaoService implements CadastrarCapacitacaoUseCase {

    private final CapacitacaoRepository capacitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistradorAuditoria auditoria;
    private final Validador<Certificacao> certificacaoValidador;
    private final Validador<Treinamento> treinamentoValidador;
    private final Validador<ExigenciaCapacitacao> exigenciaValidador;
    private final Clock clock;

    /** Usa os validadores padrão; usado pelos testes sem contexto Spring. */
    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            OperadorAtual operadorAtual) {
        this(capacitacaoRepository, null, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                new CertificacaoValidador(), new TreinamentoValidador(), new ExigenciaCapacitacaoValidador(),
                Clock.systemDefaultZone());
    }

    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            UsuarioRepository usuarioRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            OperadorAtual operadorAtual) {
        this(capacitacaoRepository, usuarioRepository, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                new CertificacaoValidador(), new TreinamentoValidador(), new ExigenciaCapacitacaoValidador(),
                Clock.systemDefaultZone());
    }

    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            Validador<Certificacao> certificacaoValidador,
            Validador<Treinamento> treinamentoValidador,
            Validador<ExigenciaCapacitacao> exigenciaValidador,
            OperadorAtual operadorAtual) {
        this(capacitacaoRepository, null, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                certificacaoValidador, treinamentoValidador, exigenciaValidador, Clock.systemDefaultZone());
    }

    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            RegistradorAuditoria auditoria,
            Validador<Certificacao> certificacaoValidador,
            Validador<Treinamento> treinamentoValidador,
            Validador<ExigenciaCapacitacao> exigenciaValidador,
            Clock clock) {
        this(capacitacaoRepository, null, auditoria, certificacaoValidador, treinamentoValidador, exigenciaValidador, clock);
    }

    @Autowired
    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            UsuarioRepository usuarioRepository,
            RegistradorAuditoria auditoria,
            Validador<Certificacao> certificacaoValidador,
            Validador<Treinamento> treinamentoValidador,
            Validador<ExigenciaCapacitacao> exigenciaValidador,
            Clock clock) {
        this.certificacaoValidador = certificacaoValidador;
        this.treinamentoValidador = treinamentoValidador;
        this.exigenciaValidador = exigenciaValidador;
        this.capacitacaoRepository = capacitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        this.clock = Objects.requireNonNull(clock, "Clock é obrigatório.");
    }

    @Override
    public CertificacaoOutputDTO cadastrarCertificacao(CadastrarCertificacaoInputDTO entrada) {
        LocalDate hoje = LocalDate.now(clock);
        Colaborador colaborador = resolverColaborador(entrada.colaboradorId());
        Certificacao nova = Certificacao.cadastrar(
                entrada.nome(), entrada.dataVencimento(), entrada.dataEmissao(), colaborador, hoje);
        // Chain of Responsibility (#265): valida antes do repository.
        certificacaoValidador.validar(nova);
        Certificacao salva = capacitacaoRepository.salvar(nova);

        auditoria.cadastro("Certificacao", salva.getId(), "Cadastro de certificação: " + salva.getNome());
        return CertificacaoOutputDTO.deDominio(salva);
    }

    @Override
    public TreinamentoOutputDTO cadastrarTreinamento(CadastrarTreinamentoInputDTO entrada) {
        List<Colaborador> colaboradores = resolverColaboradores(entrada.colaboradoresIds());
        Treinamento novo = Treinamento.cadastrar(entrada.nome(), entrada.conteudo(), entrada.instrutor(),
                entrada.dataRealizacao(), entrada.cargaHoraria(), entrada.validadeMeses(), colaboradores);
        treinamentoValidador.validar(novo);
        Treinamento salvo = capacitacaoRepository.salvar(novo);

        auditoria.cadastro("Treinamento", salvo.getId(), "Cadastro de treinamento: " + salvo.getNome());
        return TreinamentoOutputDTO.deDominio(salvo);
    }

    @Override
    public ExigenciaCapacitacaoOutputDTO definirExigencia(DefinirExigenciaInputDTO entrada) {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(null, entrada.nomeCapacitacao(),
                entrada.tipoCapacitacao(), entrada.alvoId(), entrada.alvo());
        exigenciaValidador.validar(exigencia);
        ExigenciaCapacitacao salva = capacitacaoRepository.salvar(exigencia);

        auditoria.cadastro("ExigenciaCapacitacao", salva.getId(),
                "Definição de exigência para " + salva.getAlvo() + " " + salva.getAlvoId() + ": " + salva.getNomeCapacitacao());
        return ExigenciaCapacitacaoOutputDTO.deDominio(salva);
    }

    private Colaborador resolverColaborador(Integer colaboradorId) {
        if (colaboradorId == null) {
            return null;
        }
        if (usuarioRepository == null) {
            return referenciaColaborador(colaboradorId);
        }
        return usuarioRepository.buscarPorId(colaboradorId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Colaborador não encontrado: " + colaboradorId));
    }

    private List<Colaborador> resolverColaboradores(List<Integer> colaboradoresIds) {
        if (colaboradoresIds == null || colaboradoresIds.isEmpty()) {
            return List.of();
        }
        if (colaboradoresIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Identificador de colaborador não pode ser nulo.");
        }
        return colaboradoresIds.stream()
                .distinct()
                .map(this::resolverColaborador)
                .toList();
    }

    private static Colaborador referenciaColaborador(Integer id) {
        return new Colaborador(id, "52998224725", "Colaborador " + id,
                LocalDate.of(1990, 1, 1), null, true, LocalDateTime.now(), LocalDateTime.now());
    }
}
