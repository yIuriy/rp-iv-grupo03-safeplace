package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.in.capacitacao.CadastrarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

@Service
@Transactional
public class CadastrarCapacitacaoService implements CadastrarCapacitacaoUseCase {

    private final CapacitacaoRepositoryPort capacitacaoRepository;
    private final LogAuditoriaRepositoryPort logAuditoriaRepository;

    public CadastrarCapacitacaoService(
            CapacitacaoRepositoryPort capacitacaoRepository,
            LogAuditoriaRepositoryPort logAuditoriaRepository) {
        this.capacitacaoRepository = capacitacaoRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
    }

    @Override
    public Certificacao cadastrarCertificacao(String nome, LocalDate dataVencimento, LocalDate dataEmissao, Integer colaboradorId) {
        Certificacao novaCertificacao = Certificacao.cadastrar(nome, dataVencimento, dataEmissao, colaboradorId);
        Certificacao salva = capacitacaoRepository.salvarCertificacao(novaCertificacao);

        registrarAuditoria("Certificacao", salva.getId(), "Cadastro de certificação: " + salva.getNome());
        return salva;
    }

    @Override
    public Treinamento cadastrarTreinamento(String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, List<Integer> colaboradoresIds) {
        Treinamento novoTreinamento = Treinamento.cadastrar(nome, conteudo, instrutor, dataRealizacao, cargaHoraria, colaboradoresIds);
        Treinamento salvo = capacitacaoRepository.salvarTreinamento(novoTreinamento);

        registrarAuditoria("Treinamento", salvo.getId(), "Cadastro de treinamento: " + salvo.getNome());
        return salvo;
    }

    @Override
    public ExigenciaCapacitacao definirExigenciaTarefa(String nomeCapacitacao, ExigenciaCapacitacao.TipoCapacitacao tipo, Integer tarefaId) {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(null, nomeCapacitacao, tipo, tarefaId, ExigenciaCapacitacao.TipoAlvo.TAREFA);
        ExigenciaCapacitacao salva = capacitacaoRepository.salvarExigencia(exigencia);

        registrarAuditoria("ExigenciaCapacitacao", salva.getId(), "Definição de exigência para tarefa " + tarefaId + ": " + salva.getNomeCapacitacao());
        return salva;
    }

    @Override
    public ExigenciaCapacitacao definirExigenciaEpi(String nomeCapacitacao, ExigenciaCapacitacao.TipoCapacitacao tipo, Integer epiId) {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(null, nomeCapacitacao, tipo, epiId, ExigenciaCapacitacao.TipoAlvo.EPI);
        ExigenciaCapacitacao salva = capacitacaoRepository.salvarExigencia(exigencia);

        registrarAuditoria("ExigenciaCapacitacao", salva.getId(), "Definição de exigência para EPI " + epiId + ": " + salva.getNomeCapacitacao());
        return salva;
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
