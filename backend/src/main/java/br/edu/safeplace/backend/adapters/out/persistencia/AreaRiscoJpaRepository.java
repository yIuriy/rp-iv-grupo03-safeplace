package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRiscoJpaRepository extends JpaRepository<AreaRiscoEntity, Integer> {

    @EntityGraph(attributePaths = "episObrigatorios")
    Optional<AreaRiscoEntity> findByCodigo(String codigo);

    @Override
    @EntityGraph(attributePaths = "episObrigatorios")
    List<AreaRiscoEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "episObrigatorios")
    Optional<AreaRiscoEntity> findById(Integer id);

    @EntityGraph(attributePaths = "episObrigatorios")
    List<AreaRiscoEntity> findByNivelPerigo(String nivelPerigo);
}
