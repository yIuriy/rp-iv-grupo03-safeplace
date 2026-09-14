package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/**
 * Porta de entrada do mapeamento de áreas de risco (RF05 / UC03).
 */
public interface GerenciarAreaRiscoUseCase {

    AreaRiscoOutputDTO cadastrarAreaRisco(CadastrarAreaRiscoInputDTO inputDTO);

    List<AreaRiscoOutputDTO> listar();

    /**
     * UC03, cenário alternativo I: filtrar o mapa de riscos pelo grau de perigo.
     */
    List<AreaRiscoOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo);

    AreaRiscoOutputDTO buscarPorId(Integer id);
}
