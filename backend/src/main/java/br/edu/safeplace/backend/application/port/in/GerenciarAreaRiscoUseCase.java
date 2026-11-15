/*
 * Porta de entrada do mapeamento de áreas de risco (RF05 / UC03): cadastro, atualização, consultas
 * e histórico de alterações (issue #114).
 */
package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/**
 * Porta de entrada do mapeamento de áreas de risco (RF05 / UC03).
 */
public interface GerenciarAreaRiscoUseCase {

    default AreaRiscoOutputDTO cadastrar(CadastrarAreaRiscoInputDTO inputDTO) {
        return cadastrar(inputDTO, null);
    }

    /** RNF05: o cadastro também é registrado em LogAuditoria com o operador. */
    AreaRiscoOutputDTO cadastrar(CadastrarAreaRiscoInputDTO inputDTO, String operador);

    AreaRiscoOutputDTO atualizar(Integer id, AtualizarAreaRiscoInputDTO inputDTO, String operador);

    List<AreaRiscoOutputDTO> listar();

    /**
     * UC03, cenário alternativo I: filtrar o mapa de riscos pelo grau de perigo.
     */
    List<AreaRiscoOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo);

    AreaRiscoOutputDTO buscarPorId(Integer id);

    /**
     * UC03, cenário alternativo II, passo 4: alterações registradas da área, da mais recente para a
     * mais antiga, com valor anterior, novo, data, hora e operador.
     */
    List<RegistroHistoricoOutputDTO> listarHistorico(Integer id);
}
