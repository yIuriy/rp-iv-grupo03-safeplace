package br.edu.safeplace.backend.application.port.in.capacitacao;

import java.util.List;

import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface ConsultarCapacitacaoUseCase {
    List<Certificacao> listarCertificacoesDoColaborador(Integer colaboradorId);
    List<Treinamento> listarTreinamentosDoColaborador(Integer colaboradorId);
    List<ExigenciaCapacitacao> listarExigenciasPorTarefa(Integer tarefaId);
    List<ExigenciaCapacitacao> listarExigenciasPorEpi(Integer epiId);
    PendenciasColaboradorDTO consultarPendenciasColaborador(Integer colaboradorId);
    List<String> verificarImpedimentosParaEpi(Integer colaboradorId, Integer epiId);
    List<String> verificarImpedimentosParaTarefa(Integer colaboradorId, Integer tarefaId);
    boolean colaboradorPossuiPendencias(Integer colaboradorId, Integer tarefaId);

    List<Certificacao> listarTodasCertificacoes();
    List<Treinamento> listarTodosTreinamentos();
    List<Integer> listarColaboradoresPorStatusCertificacao(StatusCertificacao status);
}
