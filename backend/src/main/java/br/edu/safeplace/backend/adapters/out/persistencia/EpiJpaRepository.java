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

    Optional<EpiEntity> findByNumeroCa(String numeroCa);

    List<EpiEntity> findAllByNumeroCa(String numeroCa);
}
