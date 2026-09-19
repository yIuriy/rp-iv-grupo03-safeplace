package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModeloEpiJpaRepository extends JpaRepository<ModeloEpiEntity, Integer> {
    Optional<ModeloEpiEntity> findByCa(Integer ca);
}
