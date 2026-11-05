package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public interface AreaRiscoJpaRepository extends JpaRepository<AreaRisco, Integer> {

    @EntityGraph(attributePaths = {"setor", "episObrigatoriosIds"})
    Optional<AreaRisco> findByCodigo(String codigo);

    @Override
    @EntityGraph(attributePaths = {"setor", "episObrigatoriosIds"})
    List<AreaRisco> findAll();

    @Override
    @EntityGraph(attributePaths = {"setor", "episObrigatoriosIds"})
    Optional<AreaRisco> findById(Integer id);

    @EntityGraph(attributePaths = {"setor", "episObrigatoriosIds"})
    List<AreaRisco> findByNivelPerigo(NivelPerigo nivelPerigo);
}
