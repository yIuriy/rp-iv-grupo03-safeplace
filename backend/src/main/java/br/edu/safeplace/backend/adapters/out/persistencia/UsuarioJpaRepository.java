package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.usuario.Colaborador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<Colaborador, Integer> {

    Optional<Colaborador> findByCpf(String cpf);

    Optional<Colaborador> findByEmail(String email);

    /**
     * Busca apenas as linhas cujo discriminador é COLABORADOR. A comparação por
     * {@code TYPE(u) = Colaborador} é exata, então Supervisor e Gestor de Segurança ficam
     * de fora mesmo herdando da mesma entidade e compartilhando a tabela.
     */
    @Query("""
            SELECT u FROM Colaborador u
            WHERE TYPE(u) = Colaborador
              AND (:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))
              AND (:cpf IS NULL OR u.cpf = :cpf)
            ORDER BY u.nome ASC
            """)
    List<Colaborador> buscarColaboradores(@Param("nome") String nome, @Param("cpf") String cpf);
}
