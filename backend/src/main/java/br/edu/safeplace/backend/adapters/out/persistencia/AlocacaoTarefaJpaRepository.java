package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlocacaoTarefaJpaRepository extends JpaRepository<AlocacaoTarefaEntity, Integer> {

    List<AlocacaoTarefaEntity> findByTarefaIdOrderByDataAlocacaoDesc(Integer tarefaId);

    List<AlocacaoTarefaEntity> findByColaboradorIdOrderByDataAlocacaoDesc(Integer colaboradorId);
}
