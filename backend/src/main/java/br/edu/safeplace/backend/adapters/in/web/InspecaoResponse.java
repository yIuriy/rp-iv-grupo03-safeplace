/*
 * Resposta HTTP de uma inspeção registrada (US09, critérios 2 e 4; issue #134): data, área, Gestor
 * responsável, itens verificados e a contagem de não conformidades.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Inspeção do histórico de uma área de risco")
public record InspecaoResponse(
        @Schema(description = "Identificador da inspeção", example = "3") Integer id,
        @Schema(description = "Área de risco inspecionada", example = "1") Integer areaRiscoId,
        @Schema(description = "Data em que a inspeção foi realizada", example = "2026-10-01") LocalDate dataInspecao,
        @Schema(description = "Identificador do Gestor de Segurança que registrou", example = "1") Integer responsavelId,
        @Schema(description = "Nome do Gestor de Segurança que registrou", example = "Gestor Demonstração") String responsavelNome,
        @Schema(description = "Quantidade de itens em não conformidade", example = "1") long naoConformidades,
        @Schema(description = "Itens verificados, na ordem do registro") List<Item> itens) {

    @Schema(description = "Item verificado na inspeção")
    public record Item(
            @Schema(description = "Identificador do item", example = "7") Integer id,
            @Schema(description = "Norma ou referência avaliada", example = "NR-26: sinalização de segurança") String descricao,
            @Schema(description = "true para conformidade, false para não conformidade", example = "false") boolean conformidade,
            @Schema(description = "Observações registradas", example = "Faixa de circulação apagada") String observacoes) {
    }

    public static InspecaoResponse fromOutputDTO(InspecaoOutputDTO dto) {
        return new InspecaoResponse(dto.id(), dto.areaRiscoId(), dto.dataInspecao(), dto.responsavelId(),
                dto.responsavelNome(), dto.naoConformidades(), dto.itens().stream()
                        .map(item -> new Item(item.id(), item.descricao(), item.conformidade(), item.observacoes()))
                        .toList());
    }
}
