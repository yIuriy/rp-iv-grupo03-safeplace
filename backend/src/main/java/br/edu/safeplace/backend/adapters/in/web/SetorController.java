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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/setores")
@Tag(name = "Setores", description = "Endpoints para gerenciamento de setores físicos da planta industrial (RF05)")
public class SetorController {

    private final GerenciarSetorUseCase useCase;

    public SetorController(GerenciarSetorUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo setor físico da planta")
    public SetorResponse criar(@Valid @RequestBody CriarSetorRequest request, Authentication authentication) {
        String responsavel = authentication != null ? authentication.getName() : "SISTEMA";
        SetorOutputDTO salvo = useCase.cadastrarSetor(request.toInputDTO(), responsavel);
        return SetorResponse.fromOutputDTO(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista todos os setores físicos da planta")
    public List<SetorResponse> listar() {
        return useCase.listarTodos().stream()
                .map(SetorResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um setor físico por ID")
    public SetorResponse buscarPorId(@PathVariable Integer id) {
        return SetorResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um setor físico da planta (RF05)")
    public SetorResponse atualizar(@PathVariable Integer id,
                                   @Valid @RequestBody AtualizarSetorRequest request,
                                   Authentication authentication) {
        String responsavel = authentication != null ? authentication.getName() : "SISTEMA";
        SetorOutputDTO salvo = useCase.atualizarSetor(id, request.toInputDTO(), responsavel);
        return SetorResponse.fromOutputDTO(salvo);
    }
}
