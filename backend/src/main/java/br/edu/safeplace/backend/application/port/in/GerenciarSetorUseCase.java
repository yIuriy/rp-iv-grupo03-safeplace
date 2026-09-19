package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;

public interface GerenciarSetorUseCase {
    SetorOutputDTO cadastrarSetor(CadastrarSetorInputDTO inputDTO);
    List<SetorOutputDTO> listar();
    default List<SetorOutputDTO> listarTodos() {
        return listar();
    }
    SetorOutputDTO buscarPorId(Integer id);
}
