package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.Operation;
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
    public TarefaResponse criar(@Valid @RequestBody CriarTarefaRequest request) {
        TarefaOutputDTO salva = useCase.cadastrarTarefa(request.toInputDTO());
        return TarefaResponse.fromOutputDTO(salva);
    }

    @GetMapping
    @Operation(summary = "Lista a matriz de periculosidade, opcionalmente filtrada pelo grau de perigo")
    public List<TarefaResponse> listar(@RequestParam(required = false) NivelPerigo nivelPerigo) {
        return useCase.listarPorNivelPerigo(nivelPerigo).stream()
                .map(TarefaResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de uma tarefa por ID")
    public TarefaResponse buscarPorId(@PathVariable Integer id) {
        return TarefaResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @PatchMapping("/{id}/classificacao")
    @Operation(summary = "Define ou reavalia o grau de periculosidade de uma tarefa existente")
    public TarefaResponse classificar(@PathVariable Integer id,
            @Valid @RequestBody ClassificarTarefaRequest request) {
        return TarefaResponse.fromOutputDTO(useCase.classificar(id, request.nivelPerigo()));
    }
}
