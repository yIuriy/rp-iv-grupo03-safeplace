package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

@Repository
public interface AlocacaoTarefaJpaRepository extends JpaRepository<AlocacaoTarefa, Integer> {
    List<AlocacaoTarefa> findByTarefa_IdOrderByDataAlocacaoDesc(Integer tarefaId);

    List<AlocacaoTarefa> findByColaborador_IdOrderByDataAlocacaoDesc(Integer colaboradorId);
}
