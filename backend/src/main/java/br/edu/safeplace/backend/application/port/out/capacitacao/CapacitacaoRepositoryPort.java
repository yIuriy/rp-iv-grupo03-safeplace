package br.edu.safeplace.backend.application.port.out.capacitacao;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface CapacitacaoRepositoryPort {
    Certificacao salvarCertificacao(Certificacao certificacao);
    Treinamento salvarTreinamento(Treinamento treinamento);
    ExigenciaCapacitacao salvarExigencia(ExigenciaCapacitacao exigencia);

    List<Certificacao> buscarCertificacoesPorColaborador(Integer colaboradorId);
    List<Treinamento> buscarTreinamentosPorColaborador(Integer colaboradorId);
    
    List<ExigenciaCapacitacao> buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId);
    
    Optional<Certificacao> buscarCertificacaoPorId(Integer id);
    Optional<Treinamento> buscarTreinamentoPorId(Integer id);
}
