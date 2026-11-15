package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

/** Implementa a porta de tarefas delegando ao repositório JPA da própria entidade de domínio (#260). */
@Repository
public class TarefaJpaAdapter implements TarefaRepository {
    private final TarefaJpaRepository tarefaRepository;

    public TarefaJpaAdapter(TarefaJpaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Override
    public Tarefa salvar(Tarefa tarefa) {
        return tarefaRepository.save(tarefa);
    }

    @Override
    public Optional<Tarefa> buscarPorId(Integer id) {
        return tarefaRepository.findById(id);
    }

    @Override
    public List<Tarefa> listar() {
        return tarefaRepository.findAll();
    }

    @Override
    public List<Tarefa> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        return tarefaRepository.findByNivelPerigo(nivelPerigo);
    }
}
