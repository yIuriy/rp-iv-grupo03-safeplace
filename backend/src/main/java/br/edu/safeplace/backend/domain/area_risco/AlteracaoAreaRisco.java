/*
 * Um campo alterado na atualização de uma área de risco, com o valor anterior e o novo, já em texto
 * (UC03, cenário alternativo II, passo 4; US05, regra 4). Não é entidade nova: é o dado que o caso de
 * uso grava na trilha de LogAuditoria para o histórico não perder o valor substituído (issue #114).
 */
package br.edu.safeplace.backend.domain.area_risco;

/**
 * {@code anterior} ou {@code novo} são nulos quando o campo estava ou ficou sem valor.
 */
public record AlteracaoAreaRisco(String campo, String anterior, String novo) {
}
