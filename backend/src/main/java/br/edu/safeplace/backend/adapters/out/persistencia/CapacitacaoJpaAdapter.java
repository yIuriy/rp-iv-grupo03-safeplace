/*
 * Adaptador JPA da porta CapacitacaoRepository (#257): delega aos repositórios Spring Data JPA das
 * próprias entidades de domínio Certificacao, Treinamento e ExigenciaCapacitacao, sem JdbcTemplate
 * e sem classes espelhadas.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

@Repository
public class CapacitacaoJpaAdapter implements CapacitacaoRepository {

    private final CertificacaoJpaRepository certificacoes;
    private final TreinamentoJpaRepository treinamentos;
    private final ExigenciaCapacitacaoJpaRepository exigencias;

    public CapacitacaoJpaAdapter(
            CertificacaoJpaRepository certificacoes,
            TreinamentoJpaRepository treinamentos,
            ExigenciaCapacitacaoJpaRepository exigencias) {
        this.certificacoes = certificacoes;
        this.treinamentos = treinamentos;
        this.exigencias = exigencias;
    }

    @Override
    public Certificacao salvarCertificacao(Certificacao certificacao) {
        return certificacoes.save(certificacao);
    }

    @Override
    public Treinamento salvarTreinamento(Treinamento treinamento) {
        return treinamentos.save(treinamento);
    }

    @Override
    public ExigenciaCapacitacao salvarExigencia(ExigenciaCapacitacao exigencia) {
        return exigencias.save(exigencia);
    }

    @Override
    public List<Certificacao> buscarCertificacoesPorColaborador(Integer colaboradorId) {
        return certificacoes.findByColaborador_IdOrderByDataVencimentoAsc(colaboradorId);
    }

    @Override
    public List<Treinamento> buscarTreinamentosPorColaborador(Integer colaboradorId) {
        return treinamentos.findByColaboradorIdOrderByDataRealizacaoDesc(colaboradorId);
    }

    @Override
    public List<ExigenciaCapacitacao> buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId) {
        return exigencias.findByAlvoAndAlvoId(tipoAlvo, alvoId);
    }

    @Override
    public Optional<Certificacao> buscarCertificacaoPorId(Integer id) {
        return certificacoes.findById(id);
    }

    @Override
    public Optional<Treinamento> buscarTreinamentoPorId(Integer id) {
        return treinamentos.findById(id);
    }

    @Override
    public List<Certificacao> listarTodasCertificacoes() {
        return certificacoes.findAllByOrderByDataVencimentoAsc();
    }

    @Override
    public List<Treinamento> listarTodosTreinamentos() {
        return treinamentos.findAllByOrderByDataRealizacaoDesc();
    }
}
