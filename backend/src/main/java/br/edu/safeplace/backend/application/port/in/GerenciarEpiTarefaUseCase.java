package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;

/**
 * Porta de entrada para vinculação e consulta de EPIs obrigatórios às tarefas (RF12 / UC08 / US12).
 */
public interface GerenciarEpiTarefaUseCase {

    /**
     * UC08 / US12: associa a lista de EPIs obrigatórios a uma tarefa cadastrada.
     * Operação restrita ao Gestor de Segurança.
     */
    TarefaOutputDTO associar(Integer tarefaId, List<Integer> epiIds);

    /**
     * UC08 / US12: consulta a lista de EPIs obrigatórios vinculados a uma tarefa cadastrada.
     */
    List<EpiOutputDTO> listarObrigatorios(Integer tarefaId);
}
