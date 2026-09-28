package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogAuditoriaJpaRepository extends JpaRepository<LogAuditoriaEntity, Integer> {
    List<LogAuditoriaEntity> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Integer entidadeId);

    @Query("SELECT l.entidadeId FROM LogAuditoriaEntity l WHERE l.responsavel = :responsavel AND l.tipoOperacao = :tipoOperacao")
    List<Integer> buscarIdsPorResponsavelETipoOperacao(@Param("responsavel") String responsavel, @Param("tipoOperacao") String tipoOperacao);
}
