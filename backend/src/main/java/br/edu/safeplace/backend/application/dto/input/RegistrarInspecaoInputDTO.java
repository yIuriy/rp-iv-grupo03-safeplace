/*
 * Dados para registrar uma inspeção de área de risco (US09, critério 1; issue #134): data em que foi
 * realizada e os itens verificados, cada um com referência avaliada, resultado e observações.
 */
package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;
import java.util.List;

public record RegistrarInspecaoInputDTO(LocalDate dataInspecao, List<Item> itens) {

    public record Item(String descricao, Boolean conformidade, String observacoes) {
    }
}
