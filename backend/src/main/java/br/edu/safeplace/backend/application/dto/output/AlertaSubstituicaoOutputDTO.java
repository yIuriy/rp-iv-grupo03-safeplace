package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.epi.AlertaSubstituicaoEpi;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para alertas preventivos de substituição e estoque de EPIs (RF21, UC10).
 */
@Schema(description = "Alerta preventivo de substituição de EPI por desgaste, CA ou estoque crítico")
public record AlertaSubstituicaoOutputDTO(
        @Schema(description = "Identificador do EPI", example = "1")
        Integer epiId,

        @Schema(description = "Nome do EPI", example = "Capacete de Segurança")
        String nomeEpi,

        @Schema(description = "Tipo do alerta (DESCARTE_PROXIMO, CA_PROXIMO_VENCIMENTO, ESTOQUE_CRITICO)", example = "DESCARTE_PROXIMO")
        String tipoAlerta,

        @Schema(description = "Descrição detalhada do alerta")
        String mensagem,

        @Schema(description = "Data limite estimada para substituição ou descarte")
        LocalDate dataLimiteEstimada,

        @Schema(description = "Dias restantes até a data limite", example = "15")
        Long diasRestantes,

        @Schema(description = "Nível de prioridade (ALTA, MEDIA, BAIXA)", example = "ALTA")
        String prioridade
) {
    public static AlertaSubstituicaoOutputDTO aPartirDe(AlertaSubstituicaoEpi alerta) {
        return new AlertaSubstituicaoOutputDTO(
                alerta.getEpiId(),
                alerta.getNomeEpi(),
                alerta.getTipoAlerta().name(),
                alerta.getMensagem(),
                alerta.getDataLimiteEstimada(),
                alerta.getDiasRestantes(),
                alerta.getPrioridade()
        );
    }
}
