package br.edu.safeplace.backend.adapters.out.persistencia;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;

/**
 * Grava notificações de ações do plano via JPA sem SQL nativo (#262).
 *
 * <p>Para alertas de {@link MotivoAvisoAcao#PRAZO}, roda em transação isolada ({@code REQUIRES_NEW}):
 * verifica existência e, em eventual corrida concorrente contra o índice único parcial
 * {@code ux_notificacoes_acao_prazo}, trata {@link DataIntegrityViolationException} como "já notificado"
 * sem marcar a transação principal do lote como rollback-only (mesmo padrão de
 * {@link SequenciaProtocoloCatInicializador}).</p>
 */
@Component
public class NotificacaoAcaoGravador {

    private final NotificacaoAcaoJpaRepository repository;

    public NotificacaoAcaoGravador(NotificacaoAcaoJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean gravarAlertaPrazoUnico(NotificacaoAcao notificacao) {
        if (repository.existsByAcaoIdAndPrazoAndMotivo(
                notificacao.getIdAcao(), notificacao.getPrazo(), MotivoAvisoAcao.PRAZO)) {
            return false;
        }
        try {
            repository.saveAndFlush(notificacao);
            return true;
        } catch (DataIntegrityViolationException jaNotificado) {
            return false;
        }
    }

    public boolean gravar(NotificacaoAcao notificacao) {
        repository.save(notificacao);
        return true;
    }
}
