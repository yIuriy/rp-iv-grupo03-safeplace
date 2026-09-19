package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;

import java.util.List;

public interface ControlarManutencaoEpiUseCase {
    EpiOutputDTO enviarParaManutencao(Integer epiId);
    ManutencaoEpiOutputDTO concluirManutencao(ConcluirManutencaoInputDTO inputDTO);
    ManutencaoEpiOutputDTO concluirManutencao(ConcluirManutencaoInputDTO inputDTO, String responsavelEmail);
    List<ManutencaoEpiOutputDTO> listarHistoricoPorEpi(Integer epiId);
}
