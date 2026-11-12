package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.epi.Epi;

public interface EpiJpaRepository extends JpaRepository<Epi, Integer> {

    @Override
    @EntityGraph(attributePaths = {"lote", "lote.modelo"})
    Optional<Epi> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {"lote", "lote.modelo"})
    List<Epi> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"lote", "lote.modelo"})
    @Query("SELECT e FROM Epi e WHERE e.codigoEPI = :id")
    Optional<Epi> findByIdWithLock(@Param("id") Integer id);

    /** O CA tem fonte única no modelo do lote (decisão #124). */
    @EntityGraph(attributePaths = {"lote", "lote.modelo"})
    @Query("SELECT e FROM Epi e WHERE e.lote.modelo.ca = :ca ORDER BY e.codigoEPI")
    List<Epi> findAllByCa(@Param("ca") Integer ca);
}
