/*
 * Porta de saída para a integração com capacitações exigida pela exceção I do UC12 e pela US11
 * (regra 3): a entrega de um EPI que exige treinamento ou certificação é impedida quando o
 * colaborador não a possui válida. O módulo de capacitações (issues #116 e #117) é a fonte dos
 * dados; esta porta só pergunta quais exigências do EPI o colaborador não atende.
 */
package br.edu.safeplace.backend.application.port.out;

import java.util.List;

public interface ImpedimentoCapacitacaoPorta {

    /**
     * Descrições das exigências de capacitação do EPI que o colaborador não atende (ausentes ou
     * vencidas). Lista vazia significa entrega liberada quanto a capacitações.
     */
    List<String> impedimentosParaEntrega(Integer colaboradorId, Integer epiId);
}
