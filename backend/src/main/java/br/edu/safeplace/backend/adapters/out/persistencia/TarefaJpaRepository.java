package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;

public interface TarefaJpaRepository extends JpaRepository<Tarefa, Integer> {

    @Override
    @EntityGraph(attributePaths = {"responsavelClassificacao", "areaRisco", "setores"})
    Optional<Tarefa> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {"responsavelClassificacao", "areaRisco", "setores"})
    List<Tarefa> findAll();

    @EntityGraph(attributePaths = {"responsavelClassificacao", "areaRisco", "setores"})
    List<Tarefa> findByNivelPerigo(NivelPerigo nivelPerigo);
}
