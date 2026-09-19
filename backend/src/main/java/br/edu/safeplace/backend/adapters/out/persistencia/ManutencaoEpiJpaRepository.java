package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManutencaoEpiJpaRepository extends JpaRepository<ManutencaoEpiEntity, Integer> {
    List<ManutencaoEpiEntity> findByEpiIdOrderByDataManutencaoDesc(Integer epiId);
}
