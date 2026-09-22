/*
 * Estado informado na devolução de um EPI (UC12, cenário alternativo I; US11, dicionário "Estado
 * na devolução"). Orienta o destino do equipamento: só APTO volta ao saldo disponível. MANUTENCAO e
 * DESCARTE não creditam o estoque; o descarte definitivo (RF14) fica no backlog.
 */
package br.edu.safeplace.backend.domain.emprestimo;

public enum EstadoDevolucao {
    APTO,
    MANUTENCAO,
    DESCARTE;

    /** Só o item apto retorna ao saldo disponível para novo empréstimo (US11, regra 5). */
    public boolean retornaAoEstoque() {
        return this == APTO;
    }
}
