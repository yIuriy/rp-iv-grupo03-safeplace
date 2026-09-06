package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Requisição para cadastro de novo EPI")
public record CriarEpiRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome do equipamento de proteção", example = "Capacete de Segurança com Aba Frontal")
        String nome,

        @NotBlank(message = "Número do CA é obrigatório")
        @Schema(description = "Número do Certificado de Aprovação (CA)", example = "12345")
        String numeroCa,

        @NotNull(message = "Quantidade inicial é obrigatória")
        @Min(value = 0, message = "Quantidade não pode ser negativa")
        @Schema(description = "Quantidade inicial em estoque", example = "25")
        Integer quantidade,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
        @Schema(description = "Limite de estoque mínimo para alertas", example = "5")
        Integer estoqueMinimo,

        @Schema(description = "Data de validade do CA (opcional para base do RF21)", example = "2027-12-31")
        LocalDate dataValidadeCa,

        @Schema(description = "Vida útil estimada em dias (opcional para base do RF21)", example = "365")
        Integer vidaUtilDias
) {
    public CadastrarEpiInputDTO toInputDTO() {
        return new CadastrarEpiInputDTO(
                nome,
                numeroCa,
                quantidade,
                estoqueMinimo,
                dataValidadeCa,
                vidaUtilDias
        );
    }
}
