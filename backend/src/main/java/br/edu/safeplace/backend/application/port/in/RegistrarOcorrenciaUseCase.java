/*
 * Porta de entrada do relato de ocorrências (UC09) e da consulta por critérios (UC05/UC09, issue #128).
 */
package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;

import java.util.List;

public interface RegistrarOcorrenciaUseCase {
    OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO);
    OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO);

    /**
     * Ocorrências que atendem aos critérios (UC05/UC09, issue #128). O Supervisor recebe só os relatos
     * que ele registrou; o Gestor, todos.
     */
    List<OcorrenciaOutputDTO> listar(FiltroOcorrenciasDTO filtro);

    default List<OcorrenciaOutputDTO> listar() {
        return listar(FiltroOcorrenciasDTO.SEM_FILTRO);
    }
}
