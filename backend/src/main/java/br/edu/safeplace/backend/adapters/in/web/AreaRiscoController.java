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
    public AreaRiscoResponse criar(@Valid @RequestBody CriarAreaRiscoRequest request) {
        AreaRiscoOutputDTO salva = useCase.cadastrarAreaRisco(request.toInputDTO());
        return AreaRiscoResponse.fromOutputDTO(salva);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma área de risco, agentes, limites e EPIs obrigatórios")
    public AreaRiscoResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarAreaRiscoRequest request,
            Authentication authentication) {
        String responsavel = authentication != null ? authentication.getName() : "SISTEMA";
        AreaRiscoOutputDTO atualizada = useCase.atualizarAreaRisco(id, request.toInputDTO(), responsavel);
        return AreaRiscoResponse.fromOutputDTO(atualizada);
    }

    @GetMapping
    @Operation(summary = "Lista o mapa de riscos, opcionalmente filtrado pelo grau de perigo")
    public List<AreaRiscoResponse> listar(@RequestParam(required = false) NivelPerigo nivelPerigo) {
        return useCase.listarPorNivelPerigo(nivelPerigo).stream()
                .map(AreaRiscoResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de uma área de risco por ID")
    public AreaRiscoResponse buscarPorId(@PathVariable Integer id) {
        return AreaRiscoResponse.fromOutputDTO(useCase.buscarPorId(id));
    }
}
