package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface TreinamentoJpaRepository extends JpaRepository<Treinamento, Integer> {

    @Override
    @EntityGraph(attributePaths = {"colaboradores"})
    Optional<Treinamento> findById(Integer id);

    @EntityGraph(attributePaths = {"colaboradores"})
    List<Treinamento> findAllByOrderByDataRealizacaoDesc();

    @EntityGraph(attributePaths = {"colaboradores"})
    @Query("SELECT DISTINCT t FROM Treinamento t WHERE EXISTS (SELECT 1 FROM t.colaboradores c WHERE c.id = :colaboradorId) ORDER BY t.dataRealizacao DESC")
    List<Treinamento> findByColaboradorIdOrderByDataRealizacaoDesc(@Param("colaboradorId") Integer colaboradorId);
}
