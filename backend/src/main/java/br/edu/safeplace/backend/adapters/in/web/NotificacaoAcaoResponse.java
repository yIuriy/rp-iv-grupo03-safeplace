package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;

public record NotificacaoAcaoResponse(
        Integer id,
        Integer idAcao,
        Integer idOcorrencia,
        String motivo,
        String responsavelTipo,
        Integer responsavelId,
        String responsavelNome,
        LocalDate prazo,
        LocalDate dataAviso,
        String mensagem,
        LocalDateTime criadaEm) {

    public static NotificacaoAcaoResponse deDominio(NotificacaoAcao notificacao) {
        return new NotificacaoAcaoResponse(
                notificacao.id(),
                notificacao.idAcao(),
                notificacao.idOcorrencia(),
                notificacao.motivo().name(),
                notificacao.responsavelTipo(),
                notificacao.responsavelId(),
                notificacao.responsavelNome(),
                notificacao.prazo(),
                notificacao.dataAviso(),
                notificacao.mensagem(),
                notificacao.criadaEm());
    }
}
