package br.edu.safeplace.backend.application.port.out;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;

/**
 * Porta de saída dos avisos sobre ações do plano (UC07; decisão 2.6 e seção 3 da correspondência do
 * plano de ação; issue #196). O aviso é interno, entregue ao Gestor de Segurança; e-mail ou outro canal
 * externo entraria como novo adaptador desta porta.
 */
public interface NotificacaoAcaoPort {

    /**
     * Entrega o aviso. O alerta de prazo é emitido uma única vez por ação e prazo: devolve falso quando
     * ele já tinha sido emitido. O aviso de reatribuição é sempre entregue.
     */
    boolean notificar(AvisoAcao aviso);

    /** Avisos entregues, do mais recente para o mais antigo. */
    List<NotificacaoAcao> listar();
}
