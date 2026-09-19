package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorJpaRepository extends JpaRepository<SetorEntity, Integer> {
    Optional<SetorEntity> findByNomeIgnoreCase(String nome);
}
