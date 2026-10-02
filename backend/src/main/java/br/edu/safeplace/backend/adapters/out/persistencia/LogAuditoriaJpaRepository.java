/*
 * Repositório Spring Data da tabela logs_auditoria: histórico por entidade e ids registrados por um
 * responsável em uma operação e tipos de entidade.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogAuditoriaJpaRepository extends JpaRepository<LogAuditoriaEntity, Integer> {
    List<LogAuditoriaEntity> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Integer entidadeId);

    @Query("SELECT l.entidadeId FROM LogAuditoriaEntity l WHERE l.responsavel = :responsavel"
            + " AND l.tipoOperacao = :tipoOperacao AND l.entidade IN :entidades")
    List<Integer> buscarIdsPorResponsavelETipoOperacao(@Param("responsavel") String responsavel,
            @Param("tipoOperacao") String tipoOperacao, @Param("entidades") Collection<String> entidades);
}
