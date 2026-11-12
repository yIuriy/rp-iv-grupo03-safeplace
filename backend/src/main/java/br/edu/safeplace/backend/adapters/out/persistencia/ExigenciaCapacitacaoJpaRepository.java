package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;

public interface ExigenciaCapacitacaoJpaRepository extends JpaRepository<ExigenciaCapacitacao, Integer> {

    List<ExigenciaCapacitacao> findByAlvoAndAlvoId(ExigenciaCapacitacao.TipoAlvo alvo, Integer alvoId);
}
