package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;

import java.util.List;

public interface RegistrarOcorrenciaUseCase {
    OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO);
    OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO);
    List<OcorrenciaOutputDTO> listar();
}
