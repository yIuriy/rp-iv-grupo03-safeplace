package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

        String situacaoRisco,
        String potencialDano,

        PlanoDeAcaoRequest planoDeAcao

) {

}
