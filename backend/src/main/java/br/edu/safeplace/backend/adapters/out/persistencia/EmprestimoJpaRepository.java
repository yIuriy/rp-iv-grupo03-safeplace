/*
 * Repositório Spring Data da tabela emprestimos.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmprestimoJpaRepository extends JpaRepository<EmprestimoEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmprestimoEntity e WHERE e.id = :id")
    Optional<EmprestimoEntity> findByIdWithLock(@Param("id") Integer id);

    List<EmprestimoEntity> findByColaboradorIdOrderByDataRetiradaDesc(Integer colaboradorId);

    List<EmprestimoEntity> findByColaboradorIdAndStatus(Integer colaboradorId, String status);

    List<EmprestimoEntity> findAllByOrderByDataRetiradaDesc();
}
