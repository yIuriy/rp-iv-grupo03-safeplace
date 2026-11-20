package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;

/** Repositório Spring Data da tabela projecoes_substituicao, sobre a própria entidade de domínio (#259). */
public interface ProjecaoSubstituicaoJpaRepository extends JpaRepository<ProjecaoSubstituicao, Integer> {

    @EntityGraph(attributePaths = {"epi", "epi.lote", "epi.lote.modelo"})
    List<ProjecaoSubstituicao> findByEpiCodigoEPIOrderByDataProjecaoDescIdDesc(Integer epiId);

    @EntityGraph(attributePaths = {"epi", "epi.lote", "epi.lote.modelo"})
    Optional<ProjecaoSubstituicao> findFirstByEpiCodigoEPIOrderByDataProjecaoDescIdDesc(Integer epiId);

    @EntityGraph(attributePaths = {"epi", "epi.lote", "epi.lote.modelo"})
    List<ProjecaoSubstituicao> findAllByOrderByDataProjecaoDescIdDesc();
}
