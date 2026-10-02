/*
 * Repositório Spring Data de ocorrências: CRUD, busca de acidente pelo protocolo CAT e consultas por
 * critérios via Specification (issue #128).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OcorrenciaJpaRepository extends JpaRepository<OcorrenciaEntity, Integer>,
        JpaSpecificationExecutor<OcorrenciaEntity> {

    @Query("SELECT a FROM AcidenteEntity a WHERE a.numeroProtocolo = :numeroProtocolo")
    Optional<AcidenteEntity> findByNumeroProtocolo(@Param("numeroProtocolo") String numeroProtocolo);
}
