package br.edu.safeplace.backend.application.port.in.capacitacao;

import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface ConsultarCapacitacaoUseCase {
    List<Certificacao> listarCertificacoesDoColaborador(Integer colaboradorId);
    List<Treinamento> listarTreinamentosDoColaborador(Integer colaboradorId);
    List<ExigenciaCapacitacao> listarExigenciasPorTarefa(Integer tarefaId);
    List<ExigenciaCapacitacao> listarExigenciasPorEpi(Integer epiId);
    boolean colaboradorPossuiPendencias(Integer colaboradorId, Integer tarefaId);
}
