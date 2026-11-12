/*
 * Repositório Spring Data da tabela inspecoes (issue #134): histórico de uma área com itens e área
 * carregados na mesma consulta, da data mais recente para a mais antiga.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.inspecao.Inspecao;

public interface InspecaoJpaRepository extends JpaRepository<Inspecao, Integer> {

    @EntityGraph(attributePaths = {"itens", "areaRisco", "responsavel"})
    List<Inspecao> findByAreaRisco_IdOrderByDataInspecaoDescIdDesc(Integer areaRiscoId);
}
