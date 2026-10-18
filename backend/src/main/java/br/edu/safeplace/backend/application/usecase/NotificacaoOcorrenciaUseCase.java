package br.edu.safeplace.backend.application.usecase;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.NotificacaoOcorrenciaPort;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;

/** Consulta das notificações de novos relatos (UC09, passo 12; issue #195). */
@Service
public class NotificacaoOcorrenciaUseCase implements ConsultarNotificacoesOcorrenciaUseCase {
    private final NotificacaoOcorrenciaPort notificacaoOcorrenciaPort;

    public NotificacaoOcorrenciaUseCase(NotificacaoOcorrenciaPort notificacaoOcorrenciaPort) {
        this.notificacaoOcorrenciaPort = Objects.requireNonNull(notificacaoOcorrenciaPort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacaoOcorrencia> listar() {
        return notificacaoOcorrenciaPort.listar();
    }
}
