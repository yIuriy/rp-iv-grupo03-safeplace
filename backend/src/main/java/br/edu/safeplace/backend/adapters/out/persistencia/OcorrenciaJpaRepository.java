package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OcorrenciaJpaRepository extends JpaRepository<OcorrenciaEntity, Integer> {

    @Query("SELECT a FROM AcidenteEntity a WHERE a.numeroProtocolo = :numeroProtocolo")
    Optional<AcidenteEntity> findByNumeroProtocolo(@Param("numeroProtocolo") String numeroProtocolo);
}
