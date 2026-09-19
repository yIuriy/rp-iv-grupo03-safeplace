package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoteEpiJpaRepository extends JpaRepository<LoteEpiEntity, Integer> {
    Optional<LoteEpiEntity> findByNumeroLote(String numeroLote);
}
