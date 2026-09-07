package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/epis")
@Tag(name = "EPIs", description = "Endpoints para gerenciamento de EPIs e controle de estoque")
public class EpiController {

    private final GerenciarEpiUseCase useCase;

    public EpiController(GerenciarEpiUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo EPI")
    public EpiResponse criar(@Valid @RequestBody CriarEpiRequest request) {
        EpiOutputDTO salvo = useCase.cadastrarEpi(request.toInputDTO());
        return EpiResponse.fromOutputDTO(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista todos os EPIs cadastrados")
    public List<EpiResponse> listar() {
        return useCase.listar().stream()
                .map(EpiResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de um EPI por ID")
    public EpiResponse buscarPorId(@PathVariable Integer id) {
        return EpiResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @PostMapping("/{id}/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma movimentação de estoque (ENTRADA ou SAIDA) para o EPI")
    public MovimentacaoEstoqueResponse movimentar(@PathVariable Integer id,
                                                  @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        MovimentacaoEstoqueOutputDTO mov = useCase.registrarMovimentacao(
                id,
                request.tipo(),
                request.quantidade(),
                request.motivo()
        );
        return MovimentacaoEstoqueResponse.fromOutputDTO(mov);
    }
}
