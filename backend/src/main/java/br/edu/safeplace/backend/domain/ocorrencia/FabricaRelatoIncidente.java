package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Fábrica concreta de relatos de {@link Incidente} (#239).
 *
 * <p>Produz a família de objetos para incidentes: a entidade {@link Incidente}, a {@link NotificacaoOcorrencia}
 * sem protocolo CAT e as informações correspondentes de auditoria.
 */
public class FabricaRelatoIncidente implements FabricaRelatoOcorrencia<RegistrarIncidenteInputDTO, Incidente> {

    @Override
    public TipoOcorrencia getTipo() {
        return TipoOcorrencia.INCIDENTE;
    }

    @Override
    public Incidente criarOcorrencia(
            RegistrarIncidenteInputDTO input,
            AreaRisco area,
            Colaborador colaborador,
            Colaborador relator,
            NumeroProtocoloCAT protocolo) {
        if (input == null) {
            throw new IllegalArgumentException("Dados de entrada de incidente não podem ser nulos.");
        }
        return new Incidente(
                null,
                input.descricao(),
                input.dataOcorrencia(),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                relator,
                null,
                null,
                input.situacaoRisco(),
                input.potencialDano()
        );
    }

    @Override
    public NotificacaoOcorrencia criarNotificacao(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            throw new IllegalArgumentException("Ocorrência não pode ser nula para gerar notificação.");
        }
        Incidente incidente = (Incidente) ocorrencia;
        String area = incidente.getArea() != null ? incidente.getArea().getNome() : null;

        StringBuilder mensagem = new StringBuilder("Novo incidente registrado");
        if (area != null) {
            mensagem.append(" na área ").append(area);
        }
        mensagem.append(". Aguarda triagem.");

        return new NotificacaoOcorrencia(
                null,
                incidente.getId(),
                TipoOcorrencia.INCIDENTE,
                null,
                area,
                mensagem.toString(),
                LocalDateTime.now()
        );
    }

    @Override
    public String descricaoAuditoria() {
        return "Registro de Incidente";
    }

    @Override
    public String entidadeAuditoria() {
        return "Incidente";
    }
}
