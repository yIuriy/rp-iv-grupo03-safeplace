/*
 * Enumerador ClassificacaoEmprestimo, com os três valores do diagrama de classes (atributo
 * Emprestimo.status). Os nomes do PNG descrevem o EPI visto pelo empréstimo: INDISPONIVEL enquanto
 * está em posse do colaborador, DEVOLUCAO_ATRASADA quando a posse passou da data prevista e
 * DISPONIVEL depois da devolução, quando o empréstimo deixa de reter o equipamento (UC12, US11).
 */
package br.edu.safeplace.backend.domain.emprestimo;

public enum ClassificacaoEmprestimo {
    /** Empréstimo encerrado: o EPI não está mais retido por este registro. */
    DISPONIVEL,
    /** Empréstimo ativo dentro do prazo: o EPI está em posse do colaborador. */
    INDISPONIVEL,
    /** Empréstimo ativo após a data prevista de devolução (US11, critério 3). */
    DEVOLUCAO_ATRASADA;

    public boolean ativo() {
        return this != DISPONIVEL;
    }
}
