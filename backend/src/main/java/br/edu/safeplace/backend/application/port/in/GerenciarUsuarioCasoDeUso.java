package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;

import java.util.List;

public interface GerenciarUsuarioCasoDeUso {

    /**
     * Cadastro genérico por perfil. Mantido para compatibilidade com o endpoint
     * {@code POST /api/usuarios}; os fluxos de RF23 devem usar
     * {@link #cadastrarSupervisor} e {@link #cadastrarColaborador}, que são específicos por papel.
     */
    UsuarioSaidaDTO cadastrarUsuario(CadastrarUsuarioEntradaDTO entrada);

    /**
     * RF23: o Gestor de Segurança cadastra supervisores. A senha inicial é gerada pelo sistema e
     * devolvida somente nesta resposta.
     */
    UsuarioSaidaDTO cadastrarSupervisor(CadastrarSupervisorInputDTO entrada);

    /**
     * RF23: o Supervisor cadastra colaboradores, que não possuem conta de acesso nem senha.
     */
    UsuarioSaidaDTO cadastrarColaborador(CadastrarColaboradorInputDTO entrada);

    List<UsuarioSaidaDTO> listarUsuarios();

    /**
     * Lista apenas as pessoas com perfil Colaborador, opcionalmente filtradas por nome ou CPF.
     */
    List<UsuarioSaidaDTO> listarColaboradores(FiltroColaboradorDTO filtro);

    UsuarioSaidaDTO buscarPorId(Integer id);

    UsuarioSaidaDTO buscarPorCpf(String cpf);
}
