/*
 * Dados de entrada para registrar a devolução de um EPI emprestado (UC12, cenário alternativo I):
 * apenas o estado do item. Data, hora e responsável são preenchidos pelo sistema.
 */
package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;

public record RegistrarDevolucaoInputDTO(EstadoDevolucao estado) {
}
