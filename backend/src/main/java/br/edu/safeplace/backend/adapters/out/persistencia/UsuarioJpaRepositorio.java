package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioJpaRepositorio extends JpaRepository<ColaboradorEntidade, Integer> {
    Optional<ColaboradorEntidade> findByCpf(String cpf);
    Optional<ColaboradorEntidade> findByEmail(String email);
}
