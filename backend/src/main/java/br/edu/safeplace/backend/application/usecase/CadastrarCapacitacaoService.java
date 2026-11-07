package br.edu.safeplace.backend.application.usecase;

import java.time.Clock;
import java.time.LocalDate;

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
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import org.springframework.beans.factory.annotation.Autowired;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.capacitacao.CertificacaoValidador;
import br.edu.safeplace.backend.validacao.capacitacao.TreinamentoValidador;
import br.edu.safeplace.backend.validacao.capacitacao.ExigenciaCapacitacaoValidador;

@Service
@Transactional
public class CadastrarCapacitacaoService implements CadastrarCapacitacaoUseCase {

    private final CapacitacaoRepository capacitacaoRepository;
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
        this(capacitacaoRepository, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
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
        this(capacitacaoRepository, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                certificacaoValidador, treinamentoValidador, exigenciaValidador, Clock.systemDefaultZone());
    }

    @Autowired
    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            RegistradorAuditoria auditoria,
            Validador<Certificacao> certificacaoValidador,
            Validador<Treinamento> treinamentoValidador,
            Validador<ExigenciaCapacitacao> exigenciaValidador,
            Clock clock) {
        this.certificacaoValidador = certificacaoValidador;
        this.treinamentoValidador = treinamentoValidador;
        this.exigenciaValidador = exigenciaValidador;
        this.capacitacaoRepository = capacitacaoRepository;
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        this.clock = java.util.Objects.requireNonNull(clock, "Clock é obrigatório.");
    }

    @Override
    public CertificacaoOutputDTO cadastrarCertificacao(CadastrarCertificacaoInputDTO entrada) {
        LocalDate hoje = LocalDate.now(clock);
        Certificacao nova = Certificacao.cadastrar(entrada.nome(), entrada.dataVencimento(), entrada.dataEmissao(), entrada.colaboradorId(), hoje);
        // Chain of Responsibility (#265): valida antes do repository.
        certificacaoValidador.validar(nova);
        Certificacao salva = capacitacaoRepository.salvarCertificacao(nova);

        auditoria.cadastro("Certificacao", salva.getId(), "Cadastro de certificação: " + salva.getNome());
        return CertificacaoOutputDTO.deDominio(salva);
    }

    @Override
    public TreinamentoOutputDTO cadastrarTreinamento(CadastrarTreinamentoInputDTO entrada) {
        Treinamento novo = Treinamento.cadastrar(entrada.nome(), entrada.conteudo(), entrada.instrutor(),
                entrada.dataRealizacao(), entrada.cargaHoraria(), entrada.validadeMeses(), entrada.colaboradoresIds());
        treinamentoValidador.validar(novo);
        Treinamento salvo = capacitacaoRepository.salvarTreinamento(novo);

        auditoria.cadastro("Treinamento", salvo.getId(), "Cadastro de treinamento: " + salvo.getNome());
        return TreinamentoOutputDTO.deDominio(salvo);
    }

    @Override
    public ExigenciaCapacitacaoOutputDTO definirExigencia(DefinirExigenciaInputDTO entrada) {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(null, entrada.nomeCapacitacao(),
                entrada.tipoCapacitacao(), entrada.alvoId(), entrada.alvo());
        exigenciaValidador.validar(exigencia);
        ExigenciaCapacitacao salva = capacitacaoRepository.salvarExigencia(exigencia);

        auditoria.cadastro("ExigenciaCapacitacao", salva.getId(),
                "Definição de exigência para " + salva.getAlvo() + " " + salva.getAlvoId() + ": " + salva.getNomeCapacitacao());
        return ExigenciaCapacitacaoOutputDTO.deDominio(salva);
    }

}
