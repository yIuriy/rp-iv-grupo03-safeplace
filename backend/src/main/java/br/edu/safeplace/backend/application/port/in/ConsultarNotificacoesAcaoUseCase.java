package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;

/** Consulta dos avisos de prazo e de reatribuição das ações do plano (UC07; issue #196). */
public interface ConsultarNotificacoesAcaoUseCase {
    List<NotificacaoAcao> listar();
}
