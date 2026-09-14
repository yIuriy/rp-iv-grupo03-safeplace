package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo usuário ou colaborador")
    public UsuarioResposta criar(@Valid @RequestBody CriarUsuarioRequisicao requisicao) {
        exigirGestorParaPerfilComAcesso(requisicao.perfil());
        UsuarioSaidaDTO salvo = casoDeUso.cadastrarUsuario(requisicao.paraDTOEntrada());
        return UsuarioResposta.aPartirDe(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários e colaboradores cadastrados")
    public List<UsuarioResposta> listar() {
        return casoDeUso.listarUsuarios().stream()
                .map(UsuarioResposta::aPartirDe)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de um usuário por ID")
    public UsuarioResposta buscarPorId(@PathVariable Integer id) {
        return UsuarioResposta.aPartirDe(casoDeUso.buscarPorId(id));
    }

    /**
     * Perfis com acesso ao sistema so podem ser criados pelo Gestor de Seguranca (RF23). A rota
     * aceita qualquer perfil no corpo, entao a verificacao precisa acontecer aqui.
     */
    private void exigirGestorParaPerfilComAcesso(Perfil perfil) {
        if (perfil == null || !perfil.exigeCredenciais()) {
            return;
        }

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        boolean gestor = autenticacao != null
                && autenticacao.isAuthenticated()
                && autenticacao.getAuthorities().stream()
                        .anyMatch(papel -> "ROLE_GESTOR_SEGURANCA".equals(papel.getAuthority()));

        if (!gestor) {
            throw new AccessDeniedException(
                    "Apenas Gestor de Segurança pode cadastrar supervisores ou gestores.");
        }
    }
}
