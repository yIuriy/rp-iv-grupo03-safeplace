package br.edu.safeplace.backend.application.usecase;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;

/** Consulta das notificações de novos relatos (UC09, passo 12; issue #195). */
@Service
public class NotificacaoOcorrenciaService implements ConsultarNotificacoesOcorrenciaUseCase {
    private final NotificadorOcorrencia notificadorOcorrencia;

    public NotificacaoOcorrenciaService(NotificadorOcorrencia notificadorOcorrencia) {
        this.notificadorOcorrencia = Objects.requireNonNull(notificadorOcorrencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacaoOcorrencia> listar() {
        return notificadorOcorrencia.listar();
    }
}
