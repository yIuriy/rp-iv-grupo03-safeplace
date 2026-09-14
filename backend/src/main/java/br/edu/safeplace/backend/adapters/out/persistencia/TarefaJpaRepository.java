package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaJpaRepository extends JpaRepository<TarefaEntity, Integer> {

    List<TarefaEntity> findByNivelPerigo(String nivelPerigo);
}
