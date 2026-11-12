package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;

@Repository
public interface AlocacaoTarefaJpaRepository extends JpaRepository<AlocacaoTarefa, Integer> {

    @Override
    @EntityGraph(attributePaths = {"tarefa", "colaborador", "responsavelAlocacao"})
    Optional<AlocacaoTarefa> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {"tarefa", "colaborador", "responsavelAlocacao"})
    List<AlocacaoTarefa> findAll();

    @EntityGraph(attributePaths = {"tarefa", "colaborador", "responsavelAlocacao"})
    List<AlocacaoTarefa> findByTarefa_IdOrderByDataAlocacaoDesc(Integer tarefaId);

    @EntityGraph(attributePaths = {"tarefa", "colaborador", "responsavelAlocacao"})
    List<AlocacaoTarefa> findByColaborador_IdOrderByDataAlocacaoDesc(Integer colaboradorId);
}
