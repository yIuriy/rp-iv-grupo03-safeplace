/*
 * Endpoints REST da matriz de periculosidade das tarefas (RF06 / UC11) em /api/tarefas: cadastro,
 * consultas, classificação e histórico das classificações, com erros documentados no OpenAPI (#114).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import br.edu.safeplace.backend.application.dto.input.AlocarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.AlocarColaboradorTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarefas")
@Tag(name = "Tarefas", description = "Endpoints para classificação de periculosidade e alocação em tarefas (RF06, RF10, UC02, UC11)")
public class TarefaController {

    private final ClassificarTarefaUseCase useCase;
    private final AlocarColaboradorTarefaUseCase alocarUseCase;

    public TarefaController(ClassificarTarefaUseCase useCase, AlocarColaboradorTarefaUseCase alocarUseCase) {
        this.useCase = useCase;
        this.alocarUseCase = alocarUseCase;
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

    @PostMapping("/{id}/alocacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Aloca um colaborador em uma tarefa classificada, validando impedimentos normativos de capacitação")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alocação confirmada e registrada"),
            @ApiResponse(responseCode = "400", description = "Dados da alocação inválidos"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou colaborador não encontrado"),
            @ApiResponse(responseCode = "409", description = "Tarefa sem grau de periculosidade classificado"),
            @ApiResponse(responseCode = "422", description = "Alocação bloqueada por certificações vencidas ou treinamentos pendentes")
    })
    public AlocacaoTarefaOutputDTO alocar(@PathVariable Integer id,
                                          @Valid @RequestBody AlocarColaboradorInputDTO request,
                                          Authentication autenticacao) {
        return alocarUseCase.alocar(id, request, autenticacao.getName());
    }

    @GetMapping("/{id}/alocacoes")
    @Operation(summary = "Lista o histórico de alocações registradas para uma tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de alocações da tarefa"),
            @ApiResponse(responseCode = "404", description = "Tarefa não existe")
    })
    public List<AlocacaoTarefaOutputDTO> listarAlocacoes(@PathVariable Integer id) {
        return alocarUseCase.listarPorTarefa(id);
    }

    @GetMapping("/{id}/aptidao/{colaboradorId}")
    @Operation(summary = "Consulta preventiva de aptidão de um colaborador para uma tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnóstico de aptidão e pendências retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou colaborador não encontrado")
    })
    public ConsultaAptidaoOutputDTO consultarAptidao(@PathVariable Integer id,
                                                    @PathVariable Integer colaboradorId) {
        return alocarUseCase.consultarAptidao(id, colaboradorId);
    }
}
