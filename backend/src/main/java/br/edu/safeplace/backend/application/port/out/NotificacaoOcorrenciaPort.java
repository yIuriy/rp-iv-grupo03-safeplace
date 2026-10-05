package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

/**
 * Porta de saída para envio de notificação interna sobre novas ocorrências relatadas
 * ao Gestor de Segurança (UC09, DS09, Issue #99).
 */
public interface NotificacaoOcorrenciaPort {

    void notificarGestor(Ocorrencia ocorrencia);
}
