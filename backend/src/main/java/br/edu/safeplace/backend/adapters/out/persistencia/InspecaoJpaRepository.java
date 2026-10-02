/*
 * Repositório Spring Data da tabela inspecoes (issue #134): histórico de uma área com itens e Gestor
 * carregados na mesma consulta, da data mais recente para a mais antiga.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspecaoJpaRepository extends JpaRepository<InspecaoEntity, Integer> {

    @EntityGraph(attributePaths = {"itens", "gestor"})
    List<InspecaoEntity> findByAreaRiscoIdOrderByDataInspecaoDescIdDesc(Integer areaRiscoId);
}
