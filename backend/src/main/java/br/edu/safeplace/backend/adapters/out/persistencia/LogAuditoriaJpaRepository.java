/*
 * Repositório Spring Data da tabela logs_auditoria: histórico por entidade, ids registrados por um
 * responsável em uma operação e tipos de entidade, e ações recentes de um responsável.
 *
 * RNF05 (issue #193): estende Repository, não JpaRepository, para expor apenas inclusão e consulta.
 * A trilha não oferece update nem delete; o banco também os recusa (V29).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LogAuditoriaJpaRepository extends Repository<LogAuditoriaEntity, Integer> {
    LogAuditoriaEntity save(LogAuditoriaEntity log);

    List<LogAuditoriaEntity> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Integer entidadeId);

    @Query("SELECT l.entidadeId FROM LogAuditoriaEntity l WHERE l.responsavel = :responsavel"
            + " AND l.tipoOperacao = :tipoOperacao AND l.entidade IN :entidades")
    List<Integer> buscarIdsPorResponsavelETipoOperacao(@Param("responsavel") String responsavel,
            @Param("tipoOperacao") String tipoOperacao, @Param("entidades") Collection<String> entidades);

    List<LogAuditoriaEntity> findByResponsavelOrderByDataHoraDescIdDesc(String responsavel, Pageable pagina);
}
