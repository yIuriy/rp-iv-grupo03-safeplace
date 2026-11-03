package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;

public record NotificacaoOcorrenciaResponse(
        Integer id,
        Integer ocorrenciaId,
        String tipoOcorrencia,
        String numeroProtocoloCAT,
        String areaNome,
        String mensagem,
        LocalDateTime criadaEm) {

    public static NotificacaoOcorrenciaResponse deDominio(NotificacaoOcorrencia notificacao) {
        return new NotificacaoOcorrenciaResponse(
                notificacao.id(),
                notificacao.ocorrenciaId(),
                notificacao.tipoOcorrencia().name(),
                notificacao.numeroProtocoloCAT() != null ? notificacao.numeroProtocoloCAT().getValor() : null,
                notificacao.areaNome(),
                notificacao.mensagem(),
                notificacao.criadaEm());
    }
}
