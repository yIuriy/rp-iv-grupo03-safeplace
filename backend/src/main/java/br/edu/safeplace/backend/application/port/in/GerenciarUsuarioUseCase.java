/*
 * Porta de entrada (interface) do módulo de usuários: declara as operações de cadastro, consulta e
 * atualização de RF23 que a camada web chama e UsuarioService implementa. Corresponde às
 * operações de Colaborador e Supervisor do diagrama de classes, movidas para o caso de uso na
 * arquitetura hexagonal.
 */

package br.edu.safeplace.backend.application.port.in;

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;

import java.util.List;

public interface GerenciarUsuarioUseCase {

    /**
     * Cadastro genérico por perfil. Mantido para compatibilidade com o endpoint
     * {@code POST /api/usuarios}; os fluxos de RF23 devem usar
     * {@link #cadastrarSupervisor} e {@link #cadastrarColaborador}, que são específicos por papel.
     */
    UsuarioOutputDTO cadastrarUsuario(CadastrarUsuarioInputDTO entrada);
    UsuarioOutputDTO cadastrarUsuario(CadastrarUsuarioInputDTO entrada, String operador);

    /**
     * RF23: o Gestor de Segurança cadastra supervisores. A senha inicial é gerada pelo sistema e
     * devolvida somente nesta resposta.
     */
    UsuarioOutputDTO cadastrarSupervisor(CadastrarSupervisorInputDTO entrada);
    UsuarioOutputDTO cadastrarSupervisor(CadastrarSupervisorInputDTO entrada, String operador);

    /**
     * RF23: Supervisor e Gestor de Segurança cadastram colaboradores, que não possuem conta de acesso nem senha.
     */
    UsuarioOutputDTO cadastrarColaborador(CadastrarColaboradorInputDTO entrada);
    UsuarioOutputDTO cadastrarColaborador(CadastrarColaboradorInputDTO entrada, String operador);

    /**
     * RF23 / issue #122: o Gestor de Segurança atualiza os dados cadastrais de um Supervisor,
     * preservando id, CPF, perfil e credenciais. Um id de outro perfil não é encontrado.
     */
    UsuarioOutputDTO atualizarSupervisor(Integer id, AtualizarPessoaInputDTO entrada);
    UsuarioOutputDTO atualizarSupervisor(Integer id, AtualizarPessoaInputDTO entrada, String operador);

    /**
     * RF23 / issue #122: Supervisor e Gestor atualizam os dados cadastrais de um Colaborador,
     * que continua sem conta de acesso.
     */
    UsuarioOutputDTO atualizarColaborador(Integer id, AtualizarPessoaInputDTO entrada);
    UsuarioOutputDTO atualizarColaborador(Integer id, AtualizarPessoaInputDTO entrada, String operador);

    List<UsuarioOutputDTO> listarUsuarios();

    /**
     * Lista apenas as pessoas com perfil Colaborador, opcionalmente filtradas por nome ou CPF.
     */
    List<UsuarioOutputDTO> listarColaboradores(FiltroColaboradorDTO filtro);

    UsuarioOutputDTO buscarPorId(Integer id);

    UsuarioOutputDTO buscarPorCpf(String cpf);

    /**
     * Perfil da pessoa autenticada: busca o cadastro pelo e-mail usado no login.
     */
    UsuarioOutputDTO buscarPorEmail(String email);
}
