package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;

public interface GerenciarSetorUseCase {
    default SetorOutputDTO cadastrarSetor(CadastrarSetorInputDTO inputDTO) {
        return cadastrarSetor(inputDTO, null);
    }

    /** RNF05: o cadastro é registrado em LogAuditoria com o operador. */
    SetorOutputDTO cadastrarSetor(CadastrarSetorInputDTO inputDTO, String operador);
    List<SetorOutputDTO> listar();
    default List<SetorOutputDTO> listarTodos() {
        return listar();
    }
    SetorOutputDTO buscarPorId(Integer id);
}
