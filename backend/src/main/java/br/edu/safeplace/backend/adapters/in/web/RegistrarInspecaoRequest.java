/*
 * Corpo de POST /api/areas-risco/{id}/inspecoes (US09, critério 1; issue #134): data da inspeção e
 * ao menos um item verificado, cada um com referência avaliada, resultado e observações opcionais.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Registro de inspeção periódica de uma área de risco (RF09, US09)")
public record RegistrarInspecaoRequest(
        @NotNull(message = "Informe a data em que a inspeção foi realizada")
        @Schema(description = "Data em que a inspeção foi realizada; não pode ser posterior a hoje", example = "2026-10-01")
        LocalDate dataInspecao,

        @NotEmpty(message = "Informe ao menos um item verificado com seu resultado")
        @Schema(description = "Itens verificados: referência avaliada, resultado e observações")
        List<@NotNull(message = "Item verificado vazio") @Valid Item> itens) {

    @Schema(description = "Item verificado na inspeção")
    public record Item(
            @NotBlank(message = "Informe a norma ou referência avaliada")
            @Schema(description = "Norma ou referência avaliada", example = "NR-26: sinalização de segurança")
            String descricao,

            @NotNull(message = "Informe se o item está em conformidade")
            @Schema(description = "true para conformidade, false para não conformidade", example = "false")
            Boolean conformidade,

            @Schema(description = "Condições verificadas e descrição da não conformidade", example = "Faixa de circulação apagada")
            String observacoes) {
    }

    public RegistrarInspecaoInputDTO toInputDTO() {
        return new RegistrarInspecaoInputDTO(dataInspecao, itens == null ? null : itens.stream()
                .map(item -> item == null ? null
                        : new RegistrarInspecaoInputDTO.Item(item.descricao(), item.conformidade(), item.observacoes()))
                .toList());
    }
}
