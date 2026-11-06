package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Fábrica concreta de relatos de {@link Acidente} (#239).
 *
 * <p>Produz a família de objetos para acidentes: a entidade {@link Acidente}, a {@link NotificacaoOcorrencia}
 * com protocolo CAT e as informações correspondentes de auditoria.
 */
public class FabricaRelatoAcidente implements FabricaRelatoOcorrencia<RegistrarAcidenteInputDTO, Acidente> {

    @Override
    public TipoOcorrencia getTipo() {
        return TipoOcorrencia.ACIDENTE;
    }

    @Override
    public Acidente criarOcorrencia(
            RegistrarAcidenteInputDTO input,
            AreaRisco area,
            Colaborador colaborador,
            Colaborador relator,
            NumeroProtocoloCAT protocolo) {
        if (input == null) {
            throw new IllegalArgumentException("Dados de entrada de acidente não podem ser nulos.");
        }
        CausaRaizAcidente causaRaiz = CausaRaizAcidente.de(input.causaRaiz());
        NivelPerigo gravidade = Acidente.lerGravidade(input.gravidade());

        return new Acidente(
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
                causaRaiz,
                input.tipo(),
                input.dano(),
                protocolo,
                input.destino(),
                gravidade
        );
    }

    @Override
    public NotificacaoOcorrencia criarNotificacao(Ocorrencia ocorrencia) {
        if (ocorrencia == null) {
            throw new IllegalArgumentException("Ocorrência não pode ser nula para gerar notificação.");
        }
        Acidente acidente = (Acidente) ocorrencia;
        String area = acidente.getArea() != null ? acidente.getArea().getNome() : null;
        NumeroProtocoloCAT protocolo = acidente.getProtocoloCAT();

        StringBuilder mensagem = new StringBuilder("Novo acidente registrado");
        if (protocolo != null) {
            mensagem.append(" (protocolo ").append(protocolo).append(")");
        }
        if (area != null) {
            mensagem.append(" na área ").append(area);
        }
        mensagem.append(". Aguarda triagem.");

        return new NotificacaoOcorrencia(
                null,
                acidente.getId(),
                TipoOcorrencia.ACIDENTE,
                protocolo,
                area,
                mensagem.toString(),
                LocalDateTime.now()
        );
    }

    @Override
    public String descricaoAuditoria() {
        return "Registro de Acidente de Trabalho";
    }

    @Override
    public String entidadeAuditoria() {
        return "Acidente";
    }
}
