/*
 * Adaptador de entrada HTTP do módulo de usuários: rotas /api/usuarios para cadastrar, listar,
 * buscar e atualizar supervisores e colaboradores (RF23). Traduz JSON em DTOs e delega tudo à
 * porta GerenciarUsuarioCasoDeUso; a permissão por papel fica no SecurityConfig.
 */

package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários, supervisores e colaboradores")
public class UsuarioControlador {

    private final GerenciarUsuarioCasoDeUso casoDeUso;

    public UsuarioControlador(GerenciarUsuarioCasoDeUso casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    /**
     * RF23: apenas o Gestor de Segurança provisiona supervisores. A restrição de papel é aplicada
     * de forma declarativa no {@code SecurityConfig}.
     */
    @PostMapping("/supervisores")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um Supervisor e devolve a senha inicial gerada pelo sistema")
    public UsuarioResposta criarSupervisor(@Valid @RequestBody CadastrarSupervisorRequisicao requisicao) {
        return UsuarioResposta.aPartirDe(casoDeUso.cadastrarSupervisor(requisicao.paraDTOEntrada()));
    }

    /**
     * RF23: Supervisor e Gestor de Segurança cadastram colaboradores, que não têm credenciais.
     */
    @PostMapping("/colaboradores")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um Colaborador, sem conta de acesso nem senha")
    public UsuarioResposta criarColaborador(@Valid @RequestBody CadastrarColaboradorRequisicao requisicao) {
        return UsuarioResposta.aPartirDe(casoDeUso.cadastrarColaborador(requisicao.paraDTOEntrada()));
    }

    @GetMapping("/colaboradores")
    @Operation(summary = "Lista os colaboradores cadastrados, com filtros opcionais por nome e CPF")
    public List<UsuarioResposta> listarColaboradores(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf) {
        return casoDeUso.listarColaboradores(new FiltroColaboradorDTO(nome, cpf)).stream()
                .map(UsuarioResposta::aPartirDe)
                .toList();
    }

    /**
     * RF23 / issue #122: só o Gestor de Segurança atualiza supervisores (regra no
     * {@code SecurityConfig}). A rota fixa o papel, então a atualização nunca converte um
     * Colaborador em Supervisor: um id de outro perfil responde 404.
     */
    @PutMapping("/supervisores/{id}")
    @Operation(summary = "Atualiza os dados cadastrais de um Supervisor, preservando CPF, perfil e credenciais")
    public UsuarioResposta atualizarSupervisor(@PathVariable Integer id,
                                               @Valid @RequestBody AtualizarSupervisorRequisicao requisicao) {
        return UsuarioResposta.aPartirDe(casoDeUso.atualizarSupervisor(id, requisicao.paraDTOEntrada()));
    }

    /**
     * RF23 / issue #122: Supervisor e Gestor de Segurança atualizam colaboradores, que seguem sem
     * credenciais.
     */
    @PutMapping("/colaboradores/{id}")
    @Operation(summary = "Atualiza os dados cadastrais de um Colaborador, que continua sem conta de acesso nem senha")
    public UsuarioResposta atualizarColaborador(@PathVariable Integer id,
                                                @Valid @RequestBody AtualizarColaboradorRequisicao requisicao) {
        return UsuarioResposta.aPartirDe(casoDeUso.atualizarColaborador(id, requisicao.paraDTOEntrada()));
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários e colaboradores cadastrados")
    public List<UsuarioResposta> listar() {
        return casoDeUso.listarUsuarios().stream()
                .map(UsuarioResposta::aPartirDe)
                .toList();
    }

    /**
     * Perfil do Gestor ou Supervisor autenticado: os dados vêm do e-mail do token, sem que o
     * cliente informe um id. Somente consulta; a atualização segue as rotas por papel de RF23.
     */
    @GetMapping("/me")
    @Operation(summary = "Consulta os dados cadastrais do usuário autenticado")
    public UsuarioResposta buscarProprioPerfil() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return UsuarioResposta.aPartirDe(casoDeUso.buscarPorEmail(autenticacao.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de um usuário por ID")
    public UsuarioResposta buscarPorId(@PathVariable Integer id) {
        return UsuarioResposta.aPartirDe(casoDeUso.buscarPorId(id));
    }
}
