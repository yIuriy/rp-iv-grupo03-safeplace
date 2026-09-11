package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EpiJpaRepository extends JpaRepository<EpiEntity, Integer> {
    Optional<EpiEntity> findByNumeroCa(String numeroCa);
}
