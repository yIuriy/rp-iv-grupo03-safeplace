package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.data.repository.Repository;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;

/** Notificações de novos relatos: só inclusão e consulta (issue #195, #261). */
public interface NotificacaoOcorrenciaJpaRepository extends Repository<NotificacaoOcorrencia, Integer> {
    NotificacaoOcorrencia save(NotificacaoOcorrencia notificacao);

    List<NotificacaoOcorrencia> findAllByOrderByCriadaEmDescIdDesc();
}
