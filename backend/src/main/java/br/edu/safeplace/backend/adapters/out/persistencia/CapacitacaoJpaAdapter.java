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
    public Certificacao salvar(Certificacao certificacao) {
        return certificacoes.save(certificacao);
    }

    @Override
    public Treinamento salvar(Treinamento treinamento) {
        return treinamentos.save(treinamento);
    }

    @Override
    public ExigenciaCapacitacao salvar(ExigenciaCapacitacao exigencia) {
        return exigencias.save(exigencia);
    }

    @Override
    public List<Certificacao> listarCertificacoes(Integer colaboradorId) {
        return certificacoes.findByColaborador_IdOrderByDataVencimentoAsc(colaboradorId);
    }

    @Override
    public List<Treinamento> listarTreinamentos(Integer colaboradorId) {
        return treinamentos.findByColaboradorIdOrderByDataRealizacaoDesc(colaboradorId);
    }

    @Override
    public List<ExigenciaCapacitacao> listarExigencias(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId) {
        return exigencias.findByAlvoAndAlvoId(tipoAlvo, alvoId);
    }

    @Override
    public Optional<Certificacao> buscarCertificacao(Integer id) {
        return certificacoes.findById(id);
    }

    @Override
    public Optional<Treinamento> buscarTreinamento(Integer id) {
        return treinamentos.findById(id);
    }

    @Override
    public List<Certificacao> listarCertificacoes() {
        return certificacoes.findAllByOrderByDataVencimentoAsc();
    }

    @Override
    public List<Treinamento> listarTreinamentos() {
        return treinamentos.findAllByOrderByDataRealizacaoDesc();
    }
}
