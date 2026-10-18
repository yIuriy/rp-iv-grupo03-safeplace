package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;

/** Consulta das notificações de novos relatos gravadas para o Gestor de Segurança (UC09, issue #195). */
public interface ConsultarNotificacoesOcorrenciaUseCase {
    List<NotificacaoOcorrencia> listar();
}
