/*
 * Inspeção registrada, como a consulta do histórico a devolve (US09, critérios 2 e 4; issue #134):
 * data, área, Gestor responsável, itens verificados e a contagem de não conformidades.
 */
package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;

public record InspecaoOutputDTO(
        Integer id,
        Integer areaRiscoId,
        LocalDate dataInspecao,
        Integer responsavelId,
        String responsavelNome,
        long naoConformidades,
        List<Item> itens) {

    public record Item(Integer id, String descricao, boolean conformidade, String observacoes) {

        static Item deDominio(ItemVerificado item) {
            return new Item(item.getId(), item.getDescricao(), item.isConformidade(), item.getObservacoes());
        }
    }

    public static InspecaoOutputDTO deDominio(Inspecao inspecao) {
        return new InspecaoOutputDTO(
                inspecao.getId(),
                inspecao.getAreaRiscoId(),
                inspecao.getDataInspecao(),
                inspecao.getResponsavel().getId(),
                inspecao.getResponsavel().getNome(),
                inspecao.contarNaoConformidades(),
                inspecao.getItens().stream().map(Item::deDominio).toList());
    }
}
