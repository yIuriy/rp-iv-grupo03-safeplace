/*
 * Endpoints REST do histórico de inspeções (RF09, US09, issue #134) sob a rota existente de áreas:
 * POST e GET em /api/areas-risco/{id}/inspecoes, exclusivos do Gestor de Segurança no SecurityConfig.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.GerenciarInspecaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/areas-risco/{areaRiscoId}/inspecoes")
@Tag(name = "Inspeções", description = "Histórico de inspeções periódicas das áreas de risco (RF09, backlog)")
public class InspecaoController {

    private final GerenciarInspecaoUseCase useCase;

    public InspecaoController(GerenciarInspecaoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma inspeção da área, com os itens verificados e seus resultados",
            description = "O Gestor autenticado fica registrado como responsável. A inspeção não é alterada depois.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inspeção incluída no histórico da área"),
            @ApiResponse(responseCode = "400", description = "Data ausente ou futura, nenhum item, item sem referência ou sem resultado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão: só o Gestor de Segurança registra"),
            @ApiResponse(responseCode = "404", description = "Área de risco não existe")
    })
    public InspecaoResponse registrar(@PathVariable Integer areaRiscoId,
            @Valid @RequestBody RegistrarInspecaoRequest request) {
        return InspecaoResponse.fromOutputDTO(useCase.registrar(areaRiscoId, request.toInputDTO()));
    }

    @GetMapping
    @Operation(summary = "Lista as inspeções da área, da data mais recente para a mais antiga")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de inspeções, vazio quando não há inspeção"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão: só o Gestor de Segurança consulta"),
            @ApiResponse(responseCode = "404", description = "Área de risco não existe")
    })
    public List<InspecaoResponse> listar(@PathVariable Integer areaRiscoId) {
        return useCase.listarPorArea(areaRiscoId).stream()
                .map(InspecaoResponse::fromOutputDTO)
                .toList();
    }
}
