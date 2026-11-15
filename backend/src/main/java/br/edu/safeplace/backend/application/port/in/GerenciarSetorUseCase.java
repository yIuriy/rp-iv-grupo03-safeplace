package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.AtualizarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;

public interface GerenciarSetorUseCase {
    default SetorOutputDTO cadastrar(CadastrarSetorInputDTO inputDTO) {
        return cadastrar(inputDTO, null);
    }

    /** RNF05: o cadastro é registrado em LogAuditoria com o operador. */
    SetorOutputDTO cadastrar(CadastrarSetorInputDTO inputDTO, String operador);

    default SetorOutputDTO atualizar(Integer id, AtualizarSetorInputDTO inputDTO) {
        return atualizar(id, inputDTO, null);
    }

    /** RNF05: a alteração é registrada em LogAuditoria com o operador. */
    SetorOutputDTO atualizar(Integer id, AtualizarSetorInputDTO inputDTO, String operador);

    List<SetorOutputDTO> listar();
    SetorOutputDTO buscarPorId(Integer id);
}
