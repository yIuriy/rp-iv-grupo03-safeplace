package br.edu.safeplace.backend.application.port.in.capacitacao;

import java.util.List;

import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface ConsultarCapacitacaoUseCase {
    List<Certificacao> listarCertificacoes(Integer colaboradorId);
    List<Treinamento> listarTreinamentos(Integer colaboradorId);
    List<ExigenciaCapacitacao> listarExigenciasPorTarefa(Integer tarefaId);
    List<ExigenciaCapacitacao> listarExigenciasPorEpi(Integer epiId);
    PendenciasColaboradorDTO consultarPendencias(Integer colaboradorId);
    List<String> listarImpedimentosEpi(Integer colaboradorId, Integer epiId);
    List<String> listarImpedimentosTarefa(Integer colaboradorId, Integer tarefaId);
    boolean possuiPendencias(Integer colaboradorId, Integer tarefaId);

    List<Certificacao> listarCertificacoes();
    List<Treinamento> listarTreinamentos();
    List<Integer> listarColaboradores(StatusCertificacao status);
}
