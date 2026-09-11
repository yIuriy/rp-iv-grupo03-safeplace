package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;

import java.util.List;

public interface GerenciarUsuarioCasoDeUso {
    UsuarioSaidaDTO cadastrarUsuario(CadastrarUsuarioEntradaDTO entrada);
    List<UsuarioSaidaDTO> listarUsuarios();
    UsuarioSaidaDTO buscarPorId(Integer id);
    UsuarioSaidaDTO buscarPorCpf(String cpf);
}
