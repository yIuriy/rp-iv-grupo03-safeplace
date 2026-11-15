package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;

import java.util.List;

public interface ControlarManutencaoEpiUseCase {
    EpiOutputDTO enviar(Integer epiId);
    ManutencaoEpiOutputDTO concluir(ConcluirManutencaoInputDTO inputDTO);
    ManutencaoEpiOutputDTO concluir(ConcluirManutencaoInputDTO inputDTO, String responsavelEmail);
    List<ManutencaoEpiOutputDTO> listarPorEpi(Integer epiId);
}
