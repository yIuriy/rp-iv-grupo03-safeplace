/*
 * Endpoints REST da matriz de periculosidade das tarefas (RF06 / UC11) em /api/tarefas: cadastro,
 * consultas, classificação e histórico das classificações, com erros documentados no OpenAPI (#114).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarefas")
@Tag(name = "Tarefas", description = "Endpoints para classificação do nível de periculosidade das tarefas (RF06)")
public class TarefaController {

    private final ClassificarTarefaUseCase useCase;

    public TarefaController(ClassificarTarefaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma nova tarefa, com ou sem classificação de periculosidade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa cadastrada"),
            @ApiResponse(responseCode = "400", description = "Descrição ausente ou grau fora do vocabulário NivelPerigo"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão ou classificação sem Gestor de Segurança identificável")
    })
    public TarefaResponse criar(@Valid @RequestBody CriarTarefaRequest request) {
        TarefaOutputDTO salva = useCase.cadastrarTarefa(request.toInputDTO());
        return TarefaResponse.fromOutputDTO(salva);
    }

    @GetMapping
    @Operation(summary = "Lista a matriz de periculosidade, opcionalmente filtrada pelo grau de perigo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matriz de tarefas, vazia quando nada corresponde ao filtro"),
            @ApiResponse(responseCode = "400", description = "Grau de perigo fora do vocabulário NivelPerigo")
    })
    public List<TarefaResponse> listar(@RequestParam(required = false) NivelPerigo nivelPerigo) {
        return useCase.listarPorNivelPerigo(nivelPerigo).stream()
                .map(TarefaResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de uma tarefa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada, com a última classificação"),
            @ApiResponse(responseCode = "404", description = "Tarefa não existe")
    })
    public TarefaResponse buscarPorId(@PathVariable Integer id) {
        return TarefaResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @PatchMapping("/{id}/classificacao")
    @Operation(summary = "Define ou reavalia o grau de periculosidade de uma tarefa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Classificação registrada com data, hora e responsável técnico"),
            @ApiResponse(responseCode = "400", description = "Grau ausente ou fora do vocabulário NivelPerigo"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão ou sem Gestor de Segurança identificável"),
            @ApiResponse(responseCode = "404", description = "Tarefa não existe")
    })
    public TarefaResponse classificar(@PathVariable Integer id,
            @Valid @RequestBody ClassificarTarefaRequest request) {
        return TarefaResponse.fromOutputDTO(useCase.classificar(id, request.nivelPerigo()));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Lista o histórico de uma tarefa, da entrada mais recente para a mais antiga",
            description = "UC11, regra 1: cadastro e cada classificação, com nível anterior e novo, data, hora e responsável técnico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico da tarefa"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão: só o Gestor de Segurança consulta o histórico"),
            @ApiResponse(responseCode = "404", description = "Tarefa não existe")
    })
    public List<RegistroHistoricoResponse> listarHistorico(@PathVariable Integer id) {
        return useCase.listarHistorico(id).stream()
                .map(RegistroHistoricoResponse::fromOutputDTO)
                .toList();
    }
}
