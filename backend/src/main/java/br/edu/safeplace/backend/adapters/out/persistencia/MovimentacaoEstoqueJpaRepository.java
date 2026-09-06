package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueJpaRepository extends JpaRepository<MovimentacaoEstoqueEntity, Integer> {
    List<MovimentacaoEstoqueEntity> findByEpiIdOrderByDataHoraDesc(Integer epiId);
}
