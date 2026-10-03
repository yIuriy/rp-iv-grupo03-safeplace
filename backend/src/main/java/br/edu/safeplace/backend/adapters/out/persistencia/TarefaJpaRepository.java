package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaJpaRepository extends JpaRepository<TarefaEntity, Integer> {

    @Override
    @EntityGraph(attributePaths = {"responsavelClassificacao", "areaRisco", "setores"})
    Optional<TarefaEntity> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {"responsavelClassificacao", "areaRisco", "setores"})
    List<TarefaEntity> findAll();

    @EntityGraph(attributePaths = {"responsavelClassificacao", "areaRisco", "setores"})
    List<TarefaEntity> findByNivelPerigo(String nivelPerigo);
}
