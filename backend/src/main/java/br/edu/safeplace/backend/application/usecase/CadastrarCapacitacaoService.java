package br.edu.safeplace.backend.application.usecase;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Service
@Transactional
public class CadastrarCapacitacaoService implements CadastrarCapacitacaoUseCase {

    private final CapacitacaoRepository capacitacaoRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    public CadastrarCapacitacaoService(
            CapacitacaoRepository capacitacaoRepository,
            LogAuditoriaRepository logAuditoriaRepository) {
        this.capacitacaoRepository = capacitacaoRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
    }

    @Override
    public CertificacaoOutputDTO cadastrarCertificacao(CadastrarCertificacaoInputDTO entrada) {
        Certificacao nova = Certificacao.cadastrar(entrada.nome(), entrada.dataVencimento(), entrada.dataEmissao(), entrada.colaboradorId());
        Certificacao salva = capacitacaoRepository.salvarCertificacao(nova);

        registrarAuditoria("Certificacao", salva.getId(), "Cadastro de certificação: " + salva.getNome());
        return CertificacaoOutputDTO.deDominio(salva);
    }

    @Override
    public TreinamentoOutputDTO cadastrarTreinamento(CadastrarTreinamentoInputDTO entrada) {
        Treinamento novo = Treinamento.cadastrar(entrada.nome(), entrada.conteudo(), entrada.instrutor(),
                entrada.dataRealizacao(), entrada.cargaHoraria(), entrada.validadeMeses(), entrada.colaboradoresIds());
        Treinamento salvo = capacitacaoRepository.salvarTreinamento(novo);

        registrarAuditoria("Treinamento", salvo.getId(), "Cadastro de treinamento: " + salvo.getNome());
        return TreinamentoOutputDTO.deDominio(salvo);
    }

    @Override
    public ExigenciaCapacitacaoOutputDTO definirExigencia(DefinirExigenciaInputDTO entrada) {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(null, entrada.nomeCapacitacao(),
                entrada.tipoCapacitacao(), entrada.alvoId(), entrada.alvo());
        ExigenciaCapacitacao salva = capacitacaoRepository.salvarExigencia(exigencia);

        registrarAuditoria("ExigenciaCapacitacao", salva.getId(),
                "Definição de exigência para " + salva.getAlvo() + " " + salva.getAlvoId() + ": " + salva.getNomeCapacitacao());
        return ExigenciaCapacitacaoOutputDTO.deDominio(salva);
    }

    private void registrarAuditoria(String entidade, Integer entidadeId, String justificativa) {
        String responsavel = resolverOperador();
        LogAuditoria log = LogAuditoria.registrarCadastro(responsavel, entidade, entidadeId, justificativa);
        logAuditoriaRepository.salvar(log);
    }

    private String resolverOperador() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "SISTEMA";
    }
}
