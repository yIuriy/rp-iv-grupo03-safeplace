package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;

/**
 * Registro de uma prorrogação aceita (UC07, cenário alternativo I). Não é entidade nova: é o dado
 * que o caso de uso grava na trilha de {@code LogAuditoria}, cujo atributo {@code justificativa}
 * guarda o motivo técnico, preservando o prazo anterior no histórico.
 */
public record ProrrogacaoPrazo(
        Integer idAcao,
        LocalDate prazoAnterior,
        LocalDate novoPrazo,
        String justificativa) {
}
