package br.edu.safeplace.backend.application.port.out;

import java.util.List;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

public interface AlocacaoTarefaRepositoryPort {

    AlocacaoTarefa salvar(AlocacaoTarefa alocacao);

    List<AlocacaoTarefa> listarPorTarefa(Integer tarefaId);

    List<AlocacaoTarefa> listarPorColaborador(Integer colaboradorId);
}
