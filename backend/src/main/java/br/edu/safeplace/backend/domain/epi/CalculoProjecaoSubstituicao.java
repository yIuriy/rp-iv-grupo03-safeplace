package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.util.List;

/**
 * Cálculo da projeção de substituição e dos alertas preventivos de EPI (RF21, UC10, DS10). O caso
 * de uso depende desta interface, e não da implementação (issue #267).
 */
public interface CalculoProjecaoSubstituicao {

    /**
     * Calcula a data estimada de descarte. As pré-condições (vida útil, setor, CA e histórico) são
     * verificadas antes pela cadeia {@code ProjecaoValidador}.
     */
    ProjecaoSubstituicao calcularProjecao(ContextoProjecao contexto);

    /** Alertas de estoque crítico, descarte próximo e CA próximo do vencimento (DS10, 2.4). */
    List<AlertaSubstituicaoEpi> emitirAlertas(Epi epi, ProjecaoSubstituicao projecao, LocalDate dataReferencia);
}
