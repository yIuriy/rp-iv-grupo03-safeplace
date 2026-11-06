/*
 * Repositório Spring Data de ocorrências (#261): CRUD sobre o domínio, busca de acidente pelo protocolo CAT
 * e consultas por critérios via Specification (issue #128).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

import java.util.Optional;

public interface OcorrenciaJpaRepository extends JpaRepository<Ocorrencia, Integer>,
        JpaSpecificationExecutor<Ocorrencia> {

    /** Compara o texto guardado no Value Object embutido (issue #230, #261). */
    @Query("SELECT a FROM Acidente a WHERE a.numeroProtocoloCAT.valor = :numeroProtocolo")
    Optional<Acidente> findByNumeroProtocolo(@Param("numeroProtocolo") String numeroProtocolo);
}
