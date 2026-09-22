/*
 * Repositório Spring Data da tabela emprestimos.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoJpaRepository extends JpaRepository<EmprestimoEntity, Integer> {

    List<EmprestimoEntity> findByColaboradorIdOrderByDataRetiradaDesc(Integer colaboradorId);

    List<EmprestimoEntity> findByColaboradorIdAndStatus(Integer colaboradorId, String status);

    List<EmprestimoEntity> findAllByOrderByDataRetiradaDesc();
}
