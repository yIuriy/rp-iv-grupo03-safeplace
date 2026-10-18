package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.repository.Repository;

/** Notificações de novos relatos: só inclusão e consulta (issue #195). */
public interface NotificacaoOcorrenciaJpaRepository extends Repository<NotificacaoOcorrenciaEntity, Integer> {
    NotificacaoOcorrenciaEntity save(NotificacaoOcorrenciaEntity notificacao);

    List<NotificacaoOcorrenciaEntity> findAllByOrderByCriadaEmDescIdDesc();
}
