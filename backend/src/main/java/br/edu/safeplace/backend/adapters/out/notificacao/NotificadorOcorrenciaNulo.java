/*
 * Null Object da porta NotificadorOcorrencia (issue #227): implementação neutra que não grava nem
 * envia nada. Substitui o "não há notificador" (antes, null e if != null em OcorrenciaService), para
 * que o caso de uso chame a porta sempre, sem condição. Não é um bean: em produção o Spring injeta
 * NotificacaoOcorrenciaInternaAdapter; esta classe serve a montagens sem notificação, como os testes.
 */
package br.edu.safeplace.backend.adapters.out.notificacao;

import java.util.List;

import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

public final class NotificadorOcorrenciaNulo implements NotificadorOcorrencia {

    /** Não notifica ninguém: o relato segue o fluxo normal, apenas sem aviso ao gestor. */
    @Override
    public void notificarGestor(Ocorrencia ocorrencia) {
        // Intencionalmente vazio: é o comportamento neutro do Null Object.
    }

    /** Sem notificações gravadas, a consulta devolve uma lista vazia, nunca null. */
    @Override
    public List<NotificacaoOcorrencia> listar() {
        return List.of();
    }
}
