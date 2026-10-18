package br.edu.safeplace.backend.application.port.out;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

/**
 * Porta de saída para envio de notificação interna sobre novas ocorrências relatadas
 * ao Gestor de Segurança (UC09, DS09, Issue #99). A notificação fica gravada e pode ser
 * consultada depois (issue #195).
 */
public interface NotificacaoOcorrenciaPort {

    void notificarGestor(Ocorrencia ocorrencia);

    /** Notificações gravadas, da mais recente para a mais antiga. */
    List<NotificacaoOcorrencia> listar();
}
