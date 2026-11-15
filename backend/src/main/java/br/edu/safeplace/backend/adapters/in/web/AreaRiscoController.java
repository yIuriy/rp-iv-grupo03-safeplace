/*
 * Endpoints REST do mapa de áreas de risco (RF05 / UC03) em /api/areas-risco: cadastro, atualização,
 * consultas e histórico de alterações. As respostas de erro ficam documentadas no OpenAPI (#114).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/areas-risco")
@Tag(name = "Áreas de risco", description = "Endpoints para mapeamento das áreas de risco do ambiente de trabalho (RF05)")
public class AreaRiscoController {

    private final GerenciarAreaRiscoUseCase useCase;

    public AreaRiscoController(GerenciarAreaRiscoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma nova área de risco com os EPIs obrigatórios de acesso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Área cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, nenhum EPI obrigatório ou setor inexistente"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão: só o Gestor de Segurança cadastra"),
            @ApiResponse(responseCode = "404", description = "EPI informado não existe no catálogo"),
            @ApiResponse(responseCode = "409", description = "Código de área já em uso (UC03, exceção I)")
    })
    public AreaRiscoResponse criar(@Valid @RequestBody CriarAreaRiscoRequest request, Authentication authentication) {
        String responsavel = authentication != null ? authentication.getName() : "SISTEMA";
        AreaRiscoOutputDTO salva = useCase.cadastrar(request.toInputDTO(), responsavel);
        return AreaRiscoResponse.fromOutputDTO(salva);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma área de risco, agentes, limites e EPIs obrigatórios",
            description = "O código não muda. Cada campo alterado é registrado no histórico com o valor anterior e o novo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área atualizada e alteração registrada no histórico"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, nenhum EPI obrigatório ou setor inexistente; nada é alterado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão: só o Gestor de Segurança atualiza"),
            @ApiResponse(responseCode = "404", description = "Área ou EPI informado não existe; nada é alterado")
    })
    public AreaRiscoResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarAreaRiscoRequest request,
            Authentication authentication) {
        String responsavel = authentication != null ? authentication.getName() : "SISTEMA";
        AreaRiscoOutputDTO atualizada = useCase.atualizar(id, request.toInputDTO(), responsavel);
        return AreaRiscoResponse.fromOutputDTO(atualizada);
    }

    @GetMapping
    @Operation(summary = "Lista o mapa de riscos, opcionalmente filtrado pelo grau de perigo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapa de riscos, vazio quando nada corresponde ao filtro"),
            @ApiResponse(responseCode = "400", description = "Grau de perigo fora do vocabulário NivelPerigo")
    })
    public List<AreaRiscoResponse> listar(@RequestParam(required = false) NivelPerigo nivelPerigo) {
        return useCase.listarPorNivelPerigo(nivelPerigo).stream()
                .map(AreaRiscoResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de uma área de risco por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área encontrada, com setor e EPIs obrigatórios"),
            @ApiResponse(responseCode = "404", description = "Área não existe")
    })
    public AreaRiscoResponse buscarPorId(@PathVariable Integer id) {
        return AreaRiscoResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Lista o histórico de alterações de uma área de risco, da mais recente para a mais antiga",
            description = "UC03, cenário alternativo II: cada entrada traz data, hora, operador e os campos alterados com valor anterior e novo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico da área, vazio quando ela nunca foi alterada"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão: só o Gestor de Segurança consulta o histórico"),
            @ApiResponse(responseCode = "404", description = "Área não existe")
    })
    public List<RegistroHistoricoResponse> listarHistorico(@PathVariable Integer id) {
        return useCase.listarHistorico(id).stream()
                .map(RegistroHistoricoResponse::fromOutputDTO)
                .toList();
    }
}
