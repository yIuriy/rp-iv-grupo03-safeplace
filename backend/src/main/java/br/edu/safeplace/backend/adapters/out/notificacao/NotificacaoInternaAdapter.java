package br.edu.safeplace.backend.adapters.out.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.NotificacaoOcorrenciaPort;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

/**
 * Adaptador de notificação interna para alertar o Gestor de Segurança sobre novos relatos
 * de acidentes e incidentes (UC09, DS09, Issue #99), sem presumir canais externos como e-mail ou SMS.
 */
@Component
public class NotificacaoInternaAdapter implements NotificacaoOcorrenciaPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoInternaAdapter.class);

    @Override
    public void notificarGestor(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            return;
        }

        String tipo = ocorrencia instanceof Acidente ? "ACIDENTE" : "INCIDENTE";
        String protocolo = (ocorrencia instanceof Acidente acidente && acidente.getNumeroProtocoloCAT() != null)
                ? acidente.getNumeroProtocoloCAT()
                : "SEM PROTOCOLO";
        String area = ocorrencia.getArea() != null ? ocorrencia.getArea().getNome() : "ÁREA NÃO INFORMADA";

        log.info("[NOTIFICAÇÃO GESTOR] Novo relato registrado: Tipo={}, Protocolo={}, Área={}, ID={}",
                tipo, protocolo, area, ocorrencia.getId());
    }
}
