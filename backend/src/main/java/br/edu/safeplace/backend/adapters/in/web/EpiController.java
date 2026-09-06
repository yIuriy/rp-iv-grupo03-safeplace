package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
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
        StatusEpi statusInicial = request.quantidade() > 0 ? StatusEpi.DISPONIVEL : StatusEpi.ESGOTADO;

        Epi novoEpi = new Epi(
                null,
                request.nome(),
                request.numeroCa(),
                request.quantidade(),
                request.estoqueMinimo(),
                statusInicial,
                request.dataValidadeCa(),
                request.vidaUtilDias()
        );

        Epi salvo = useCase.cadastrarEpi(novoEpi);
        return EpiResponse.fromDomain(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista todos os EPIs cadastrados")
    public List<EpiResponse> listar() {
        return useCase.listar().stream()
                .map(EpiResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de um EPI por ID")
    public EpiResponse buscarPorId(@PathVariable Integer id) {
        return EpiResponse.fromDomain(useCase.buscarPorId(id));
    }

    @PostMapping("/{id}/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma movimentação de estoque (ENTRADA ou SAIDA) para o EPI")
    public MovimentacaoEstoqueResponse movimentar(@PathVariable Integer id,
                                                  @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        MovimentacaoEstoque mov = useCase.registrarMovimentacao(
                id,
                request.tipo(),
                request.quantidade(),
                request.motivo()
        );

        Epi atualizado = useCase.buscarPorId(id);
        return MovimentacaoEstoqueResponse.fromDomain(mov, atualizado.getQuantidade());
    }
}
