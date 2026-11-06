package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.epi.ModeloEPI;

public interface ModeloEpiJpaRepository extends JpaRepository<ModeloEPI, Integer> {
    /** Registros anteriores à V28 podem ter o mesmo CA com validades diferentes; prevalece o mais antigo. */
    Optional<ModeloEPI> findFirstByCaOrderByIdAsc(Integer ca);
}
