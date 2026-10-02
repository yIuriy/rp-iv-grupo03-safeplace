/*
 * Porta de saída do histórico de inspeções (RF09, issue #134): grava uma inspeção com seus itens e
 * lista as inspeções de uma área, da data mais recente para a mais antiga.
 */
package br.edu.safeplace.backend.application.port.out;

import java.util.List;

import br.edu.safeplace.backend.domain.inspecao.Inspecao;

public interface InspecaoRepositoryPort {

    Inspecao salvar(Inspecao inspecao);

    List<Inspecao> listarPorArea(Integer areaRiscoId);
}
