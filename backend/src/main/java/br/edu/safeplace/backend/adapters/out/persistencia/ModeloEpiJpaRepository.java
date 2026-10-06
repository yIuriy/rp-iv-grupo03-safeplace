package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModeloEpiJpaRepository extends JpaRepository<ModeloEpiEntity, Integer> {
    /** Registros anteriores à V28 podem ter o mesmo CA com validades diferentes; prevalece o mais antigo. */
    Optional<ModeloEpiEntity> findFirstByCaOrderByIdAsc(Integer ca);
}
