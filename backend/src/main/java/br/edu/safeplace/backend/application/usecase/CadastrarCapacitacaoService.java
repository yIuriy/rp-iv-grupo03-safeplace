package br.edu.safeplace.backend.application.usecase;

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
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import org.springframework.beans.factory.annotation.Autowired;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.capacitacao.CertificacaoValidador;
import br.edu.safeplace.backend.validacao.capacitacao.TreinamentoValidador;
import br.edu.safeplace.backend.validacao.capacitacao.ExigenciaCapacitacaoValidador;

@Service
@Transactional
public class CadastrarCapacitacaoService implements CadastrarCapacitacaoUseCase {

    private final CapacitacaoRepository capacitacaoRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final OperadorAtual operadorAtual;
    private final Validador<Certificacao> certificacaoValidador;
    private final Validador<Treinamento> treinamentoValidador;
    private final Validador<ExigenciaCapacitacao> exigenciaValidador;

    /** Usa os validadores padrão; usado pelos testes sem contexto Spring. */
    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            OperadorAtual operadorAtual) {
        this(capacitacaoRepository, logAuditoriaRepository, new CertificacaoValidador(),
                new TreinamentoValidador(), new ExigenciaCapacitacaoValidador(), operadorAtual);
    }

    @Autowired
    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            Validador<Certificacao> certificacaoValidador,
            Validador<Treinamento> treinamentoValidador,
            Validador<ExigenciaCapacitacao> exigenciaValidador,
            OperadorAtual operadorAtual) {
        this.certificacaoValidador = certificacaoValidador;
        this.treinamentoValidador = treinamentoValidador;
        this.exigenciaValidador = exigenciaValidador;
        this.capacitacaoRepository = capacitacaoRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        // Adapter (#236): o operador vem da porta OperadorAutenticado.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
    }

    @Override
    public CertificacaoOutputDTO cadastrarCertificacao(CadastrarCertificacaoInputDTO entrada) {
        Certificacao nova = Certificacao.cadastrar(entrada.nome(), entrada.dataVencimento(), entrada.dataEmissao(), entrada.colaboradorId());
        // Chain of Responsibility (#265): valida antes do repository.
        certificacaoValidador.validar(nova);
        Certificacao salva = capacitacaoRepository.salvarCertificacao(nova);

        registrarAuditoria("Certificacao", salva.getId(), "Cadastro de certificação: " + salva.getNome());
        return CertificacaoOutputDTO.deDominio(salva);
    }

    @Override
    public TreinamentoOutputDTO cadastrarTreinamento(CadastrarTreinamentoInputDTO entrada) {
        Treinamento novo = Treinamento.cadastrar(entrada.nome(), entrada.conteudo(), entrada.instrutor(),
                entrada.dataRealizacao(), entrada.cargaHoraria(), entrada.validadeMeses(), entrada.colaboradoresIds());
        treinamentoValidador.validar(novo);
        Treinamento salvo = capacitacaoRepository.salvarTreinamento(novo);

        registrarAuditoria("Treinamento", salvo.getId(), "Cadastro de treinamento: " + salvo.getNome());
        return TreinamentoOutputDTO.deDominio(salvo);
    }

    @Override
    public ExigenciaCapacitacaoOutputDTO definirExigencia(DefinirExigenciaInputDTO entrada) {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(null, entrada.nomeCapacitacao(),
                entrada.tipoCapacitacao(), entrada.alvoId(), entrada.alvo());
        exigenciaValidador.validar(exigencia);
        ExigenciaCapacitacao salva = capacitacaoRepository.salvarExigencia(exigencia);

        registrarAuditoria("ExigenciaCapacitacao", salva.getId(),
                "Definição de exigência para " + salva.getAlvo() + " " + salva.getAlvoId() + ": " + salva.getNomeCapacitacao());
        return ExigenciaCapacitacaoOutputDTO.deDominio(salva);
    }

    private void registrarAuditoria(String entidade, Integer entidadeId, String justificativa) {
        LogAuditoria log = LogAuditoria.registrarCadastro(
                operadorAtual.emailOuSistema(), entidade, entidadeId, justificativa);
        logAuditoriaRepository.salvar(log);
    }
}
