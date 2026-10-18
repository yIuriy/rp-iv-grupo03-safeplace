package br.edu.safeplace.backend.adapters.out.notificacao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoOcorrenciaEntity;
import br.edu.safeplace.backend.adapters.out.persistencia.NotificacaoOcorrenciaJpaRepository;
import br.edu.safeplace.backend.application.port.out.NotificacaoOcorrenciaPort;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

/**
 * Adaptador de notificação interna para alertar o Gestor de Segurança sobre novos relatos
 * de acidentes e incidentes (UC09, DS09, Issue #99), sem presumir canais externos como e-mail ou SMS.
 * A notificação é gravada em {@code notificacoes_ocorrencia}, na mesma transação do relato, e pode
 * ser consultada em {@code GET /api/notificacoes} (issue #195).
 */
@Component
public class NotificacaoInternaAdapter implements NotificacaoOcorrenciaPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoInternaAdapter.class);

    private final NotificacaoOcorrenciaJpaRepository repository;

    public NotificacaoInternaAdapter(NotificacaoOcorrenciaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void notificarGestor(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            return;
        }

        NotificacaoOcorrencia notificacao = NotificacaoOcorrencia.deNovoRelato(ocorrencia);
        repository.save(NotificacaoOcorrenciaEntity.deDominio(notificacao));

        log.info("[NOTIFICAÇÃO GESTOR] {} ID={}", notificacao.mensagem(), notificacao.ocorrenciaId());
    }

    @Override
    public List<NotificacaoOcorrencia> listar() {
        return repository.findAllByOrderByCriadaEmDescIdDesc().stream()
                .map(NotificacaoOcorrenciaEntity::paraDominio)
                .toList();
    }
}
