package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.EpiResumoOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identificação enxuta de um EPI vinculado a outro cadastro")
public record EpiResumoResponse(
        @Schema(description = "Identificador único do EPI", example = "1") Integer id,

        @Schema(description = "Nome do equipamento", example = "Capacete de Segurança com Aba Frontal") String nome,

        @Schema(description = "Número do CA", example = "12345") String numeroCa) {

    public static EpiResumoResponse fromOutputDTO(EpiResumoOutputDTO dto) {
        return new EpiResumoResponse(dto.id(), dto.nome(), dto.numeroCa());
    }
}
