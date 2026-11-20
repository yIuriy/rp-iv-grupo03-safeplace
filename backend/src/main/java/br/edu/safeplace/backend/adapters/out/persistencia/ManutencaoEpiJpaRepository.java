package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;

/** Repositório Spring Data da tabela manutencoes_epi, sobre a própria entidade de domínio (#259). */
public interface ManutencaoEpiJpaRepository extends JpaRepository<ManutencaoEpi, Integer> {

    @Override
    @EntityGraph(attributePaths = {"epi", "epi.lote", "epi.lote.modelo", "responsavel"})
    Optional<ManutencaoEpi> findById(Integer id);

    @EntityGraph(attributePaths = {"epi", "epi.lote", "epi.lote.modelo", "responsavel"})
    @Query("SELECT m FROM ManutencaoEpi m WHERE m.epi.codigoEPI = :epiId ORDER BY m.dataManutencao DESC")
    List<ManutencaoEpi> listarPorEpi(@Param("epiId") Integer epiId);
}
