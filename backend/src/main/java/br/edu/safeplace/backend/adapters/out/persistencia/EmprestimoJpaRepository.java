/*
 * Repositório Spring Data da tabela emprestimos, sobre a própria entidade de domínio (#260).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;

public interface EmprestimoJpaRepository extends JpaRepository<Emprestimo, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Emprestimo e WHERE e.id = :id")
    Optional<Emprestimo> findByIdWithLock(@Param("id") Integer id);

    @Query("SELECT e FROM Emprestimo e WHERE e.colaborador.id = :colaboradorId ORDER BY e.dataRetirada DESC")
    List<Emprestimo> listarPorColaborador(@Param("colaboradorId") Integer colaboradorId);

    @Query("SELECT e FROM Emprestimo e WHERE e.colaborador.id = :colaboradorId AND e.status = :status")
    List<Emprestimo> listarPorColaboradorEStatus(@Param("colaboradorId") Integer colaboradorId,
            @Param("status") ClassificacaoEmprestimo status);

    @Query("SELECT e FROM Emprestimo e WHERE e.epi.codigoEPI = :epiId ORDER BY e.dataRetirada DESC")
    List<Emprestimo> listarPorEpi(@Param("epiId") Integer epiId);

    List<Emprestimo> findAllByOrderByDataRetiradaDesc();
}
