package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoDeAcaoJpaRepository extends JpaRepository<PlanoDeAcaoEntity, Integer> {

    List<PlanoDeAcaoEntity> findByOcorrenciaIdOrderByIdAsc(Integer ocorrenciaId);

    /** Ações com alerta ativo na situação informada, para a verificação periódica de prazo (#196). */
    List<PlanoDeAcaoEntity> findByAlertaAtivoTrueAndStatusOrderByIdAsc(String status);
}
