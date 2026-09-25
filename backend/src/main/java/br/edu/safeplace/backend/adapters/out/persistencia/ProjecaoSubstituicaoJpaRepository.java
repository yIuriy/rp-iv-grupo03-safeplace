package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjecaoSubstituicaoJpaRepository extends JpaRepository<ProjecaoSubstituicaoEntity, Integer> {

    List<ProjecaoSubstituicaoEntity> findByEpiIdOrderByDataProjecaoDescIdDesc(Integer epiId);

    Optional<ProjecaoSubstituicaoEntity> findFirstByEpiIdOrderByDataProjecaoDescIdDesc(Integer epiId);

    List<ProjecaoSubstituicaoEntity> findAllByOrderByDataProjecaoDescIdDesc();
}
