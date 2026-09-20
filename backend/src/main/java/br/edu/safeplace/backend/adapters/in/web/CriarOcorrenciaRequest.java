package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CriarOcorrenciaRequest(
        @NotBlank String tipoOcorrencia,
        @NotNull LocalDateTime dataOcorrencia,
        @NotBlank String local,
        @NotBlank String descricao,

        String causaRaiz,
        String tipo,
        String dano,
        String numeroProtocolo,
        String destino,
        String gravidade,

        String situacaoRisco,
        String potencialDano,

        PlanoDeAcaoRequest planoDeAcao
) {
    public RegistrarAcidenteInputDTO toAcidenteInputDTO() {
        return new RegistrarAcidenteInputDTO(
                dataOcorrencia,
                local,
                descricao,
                planoDeAcao != null ? planoDeAcao.toDomain() : null,
                causaRaiz,
                tipo,
                dano,
                numeroProtocolo,
                destino,
                converterGravidade(gravidade)
        );
    }

    public RegistrarIncidenteInputDTO toIncidenteInputDTO() {
        return new RegistrarIncidenteInputDTO(
                dataOcorrencia,
                local,
                descricao,
                planoDeAcao != null ? planoDeAcao.toDomain() : null,
                situacaoRisco,
                potencialDano
        );
    }

    private static NivelPerigo converterGravidade(String gravidade) {
        if (gravidade == null || gravidade.isBlank()) {
            return NivelPerigo.BAIXO;
        }
        try {
            return NivelPerigo.valueOf(gravidade.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NivelPerigo.BAIXO;
        }
    }
}
