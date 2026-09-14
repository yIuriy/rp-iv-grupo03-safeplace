package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

public interface TarefaRepositoryPort {

    Tarefa salvar(Tarefa tarefa);

    Optional<Tarefa> buscarPorId(Integer id);

    List<Tarefa> listarTodas();

    List<Tarefa> listarPorNivelPerigo(NivelPerigo nivelPerigo);
}
