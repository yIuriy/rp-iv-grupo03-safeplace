/*
 * Porta de entrada do histórico de inspeções das áreas de risco (RF09, US09, issue #134): registrar
 * uma inspeção e consultar as inspeções de uma área.
 */
package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;

public interface GerenciarInspecaoUseCase {

    /**
     * US09, critério 1: inclui a inspeção no histórico da área, com o Gestor autenticado como responsável.
     */
    InspecaoOutputDTO registrar(Integer areaRiscoId, RegistrarInspecaoInputDTO inputDTO);

    /**
     * US09, critérios 2 e 3: todas as inspeções da área, da data mais recente para a mais antiga.
     */
    List<InspecaoOutputDTO> listarPorArea(Integer areaRiscoId);
}
