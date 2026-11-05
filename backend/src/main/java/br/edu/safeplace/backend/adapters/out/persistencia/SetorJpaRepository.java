package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.area_risco.Setor;

public interface SetorJpaRepository extends JpaRepository<Setor, Integer> {
    Optional<Setor> findByNomeIgnoreCase(String nome);
}
