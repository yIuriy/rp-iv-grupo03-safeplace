package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EpiJpaRepository extends JpaRepository<EpiEntity, Integer> {
}
