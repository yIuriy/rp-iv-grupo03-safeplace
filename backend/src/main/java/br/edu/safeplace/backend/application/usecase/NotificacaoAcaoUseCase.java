package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesAcaoUseCase;
import br.edu.safeplace.backend.application.port.in.EmitirAlertasPrazoUseCase;
import br.edu.safeplace.backend.application.port.out.NotificacaoAcaoPort;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepositoryPort;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

/**
 * Alertas de prazo e consulta dos avisos das ações do plano (UC07, regra 2; US07, critério 5; issue
 * #196). A regra de quando o alerta é devido fica no domínio ({@code PlanoDeAcao.emitirAvisoPrazo});
 * a emissão única por ação e prazo é garantida pela porta de notificação.
 */
@Service
public class NotificacaoAcaoUseCase implements EmitirAlertasPrazoUseCase, ConsultarNotificacoesAcaoUseCase {
    private final PlanoDeAcaoRepositoryPort planoDeAcaoRepository;
    private final NotificacaoAcaoPort notificacaoAcaoPort;

    public NotificacaoAcaoUseCase(PlanoDeAcaoRepositoryPort planoDeAcaoRepository, NotificacaoAcaoPort notificacaoAcaoPort) {
        this.planoDeAcaoRepository = Objects.requireNonNull(planoDeAcaoRepository);
        this.notificacaoAcaoPort = Objects.requireNonNull(notificacaoAcaoPort);
    }

    @Override
    @Transactional
    public int emitirAlertasDePrazo(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            throw new IllegalArgumentException("A data de referência é obrigatória.");
        }
        int emitidos = 0;
        for (PlanoDeAcao acao : planoDeAcaoRepository.listarAbertasComAlertaAtivo()) {
            boolean novo = acao.emitirAvisoPrazo(dataReferencia)
                    .map(notificacaoAcaoPort::notificar)
                    .orElse(false);
            if (novo) {
                emitidos++;
            }
        }
        return emitidos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacaoAcao> listar() {
        return notificacaoAcaoPort.listar();
    }
}
