package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarUsuarioCasoDeUso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
        if (requisicao.perfil() == br.edu.safeplace.backend.domain.usuario.Perfil.SUPERVISOR
                || requisicao.perfil() == br.edu.safeplace.backend.domain.usuario.Perfil.GESTOR_SEGURANCA) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty()) {
                boolean isGestor = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR_SEGURANCA"));
                if (!isGestor) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Apenas Gestor de Segurança pode cadastrar supervisores ou gestores.");
                }
            }
        }
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
}
