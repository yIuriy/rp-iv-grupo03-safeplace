package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.epi.AlertaSubstituicaoEpi;

/**
 * DTO para alertas preventivos de substituição e estoque de EPIs (RF21, UC10).
 */
public record AlertaSubstituicaoOutputDTO(
        Integer epiId,
        String nomeEpi,
        String tipoAlerta,
        String mensagem,
        LocalDate dataLimiteEstimada,
        Long diasRestantes,
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
