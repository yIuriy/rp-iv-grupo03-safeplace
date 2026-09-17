package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
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

    /**
     * RF23 / issue #122: o Gestor de Segurança atualiza os dados cadastrais de um Supervisor,
     * preservando id, CPF, perfil e credenciais. Um id de outro perfil não é encontrado.
     */
    UsuarioSaidaDTO atualizarSupervisor(Integer id, AtualizarPessoaInputDTO entrada);

    /**
     * RF23 / issue #122: Supervisor e Gestor atualizam os dados cadastrais de um Colaborador,
     * que continua sem conta de acesso.
     */
    UsuarioSaidaDTO atualizarColaborador(Integer id, AtualizarPessoaInputDTO entrada);

    List<UsuarioSaidaDTO> listarUsuarios();

    /**
     * Lista apenas as pessoas com perfil Colaborador, opcionalmente filtradas por nome ou CPF.
     */
    List<UsuarioSaidaDTO> listarColaboradores(FiltroColaboradorDTO filtro);

    UsuarioSaidaDTO buscarPorId(Integer id);

    UsuarioSaidaDTO buscarPorCpf(String cpf);
}
