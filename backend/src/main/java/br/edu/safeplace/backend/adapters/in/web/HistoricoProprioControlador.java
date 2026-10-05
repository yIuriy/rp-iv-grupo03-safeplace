/*
 * Rota do histórico do próprio perfil (RNF05): GET /api/usuarios/me/historico devolve as ações que o
 * Gestor ou o Supervisor autenticado registrou, identificando a pessoa pelo e-mail do token.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.ConsultarHistoricoProprioCasoDeUso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios/me/historico")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários, supervisores e colaboradores")
public class HistoricoProprioControlador {

    private final ConsultarHistoricoProprioCasoDeUso casoDeUso;

    public HistoricoProprioControlador(ConsultarHistoricoProprioCasoDeUso casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @GetMapping
    @Operation(summary = "Lista as ações mais recentes registradas pelo usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Ações da mais recente para a mais antiga")
    @ApiResponse(responseCode = "401", description = "Sem autenticação")
    public List<AcaoUsuarioResponse> listar(
            @Parameter(description = "Quantidade máxima de ações, de 1 a 100")
            @RequestParam(defaultValue = "" + ConsultarHistoricoProprioCasoDeUso.LIMITE_PADRAO) int limite) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return casoDeUso.listarAcoes(autenticacao.getName(), limite).stream()
                .map(AcaoUsuarioResponse::fromOutputDTO)
                .toList();
    }
}
