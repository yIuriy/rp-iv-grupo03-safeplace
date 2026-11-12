package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;

public interface MovimentacaoEstoqueJpaRepository extends JpaRepository<MovimentacaoEstoque, Integer> {
    @EntityGraph(attributePaths = {"epi", "lote", "responsavel"})
    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.epi.codigoEPI = :epiId ORDER BY m.dataHora DESC")
    List<MovimentacaoEstoque> findByEpiIdOrderByDataHoraDesc(@Param("epiId") Integer epiId);
}
