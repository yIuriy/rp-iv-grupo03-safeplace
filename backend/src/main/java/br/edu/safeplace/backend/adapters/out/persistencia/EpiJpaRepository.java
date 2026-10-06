package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpiJpaRepository extends JpaRepository<EpiEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EpiEntity e WHERE e.id = :id")
    Optional<EpiEntity> findByIdWithLock(@Param("id") Integer id);

    /** O CA tem fonte única no modelo do lote (decisão #124). */
    @Query("SELECT e FROM EpiEntity e WHERE e.lote.modelo.ca = :ca ORDER BY e.id")
    List<EpiEntity> findAllByCa(@Param("ca") Integer ca);
}
