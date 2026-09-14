package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

@Repository
public class TarefaJpaAdapter implements TarefaRepositoryPort {

    private final TarefaJpaRepository tarefaRepository;

    public TarefaJpaAdapter(TarefaJpaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Override
    public Tarefa salvar(Tarefa tarefa) {
        return toDomain(tarefaRepository.save(toEntity(tarefa)));
    }

    @Override
    public Optional<Tarefa> buscarPorId(Integer id) {
        return tarefaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Tarefa> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        return tarefaRepository.findByNivelPerigo(nivelPerigo.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private TarefaEntity toEntity(Tarefa domain) {
        return new TarefaEntity(
                domain.getId(),
                domain.getDescricao(),
                domain.getNivelPerigo() == null ? null : domain.getNivelPerigo().name(),
                domain.getDataClassificacao());
    }

    private Tarefa toDomain(TarefaEntity entity) {
        return new Tarefa(
                entity.getId(),
                entity.getDescricao(),
                entity.getNivelPerigo() == null ? null : NivelPerigo.valueOf(entity.getNivelPerigo()),
                entity.getDataClassificacao());
    }
}
