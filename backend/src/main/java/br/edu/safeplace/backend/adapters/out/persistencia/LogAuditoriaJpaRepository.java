package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaJpaRepository extends JpaRepository<LogAuditoriaEntity, Integer> {
    List<LogAuditoriaEntity> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Integer entidadeId);
}
