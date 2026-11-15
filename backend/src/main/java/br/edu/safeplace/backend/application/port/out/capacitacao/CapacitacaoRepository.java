package br.edu.safeplace.backend.application.port.out.capacitacao;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface CapacitacaoRepository {
    Certificacao salvar(Certificacao certificacao);
    Treinamento salvar(Treinamento treinamento);
    ExigenciaCapacitacao salvar(ExigenciaCapacitacao exigencia);

    List<Certificacao> listarCertificacoes(Integer colaboradorId);
    List<Treinamento> listarTreinamentos(Integer colaboradorId);
    
    List<ExigenciaCapacitacao> listarExigencias(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId);
    
    Optional<Certificacao> buscarCertificacao(Integer id);
    Optional<Treinamento> buscarTreinamento(Integer id);

    List<Certificacao> listarCertificacoes();
    List<Treinamento> listarTreinamentos();
}
