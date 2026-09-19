package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarManutencaoEpiUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/epis")
@Tag(name = "EPIs", description = "Endpoints para gerenciamento de EPIs e controle de estoque")
public class EpiController {

    private final GerenciarEpiUseCase useCase;
    private final ControlarManutencaoEpiUseCase manutencaoUseCase;

    public EpiController(GerenciarEpiUseCase useCase,
                         ControlarManutencaoEpiUseCase manutencaoUseCase) {
        this.useCase = useCase;
        this.manutencaoUseCase = manutencaoUseCase;
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
                                                  @Valid @RequestBody MovimentacaoEstoqueRequest request,
                                                  Authentication autenticacao) {
        MovimentacaoEstoqueOutputDTO mov = useCase.registrarMovimentacao(
                id,
                request.tipo(),
                request.quantidade(),
                request.motivo(),
                autenticacao.getName()
        );
        return MovimentacaoEstoqueResponse.fromOutputDTO(mov);
    }

    @GetMapping("/{id}/movimentacoes")
    @Operation(summary = "Consulta o histórico de movimentações de estoque do EPI")
    public List<MovimentacaoEstoqueResponse> buscarHistorico(@PathVariable Integer id) {
        return useCase.buscarHistorico(id).stream()
                .map(MovimentacaoEstoqueResponse::fromOutputDTO)
                .toList();
    }

    @PostMapping("/{id}/manutencoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra manutenção concluída para o EPI")
    public ManutencaoEpiResponse registrarManutencao(@PathVariable Integer id,
                                                      @Valid @RequestBody RegistrarManutencaoRequest request,
                                                      Authentication autenticacao) {
        ManutencaoEpiOutputDTO manutencao = manutencaoUseCase.concluirManutencao(
                request.toInputDTO(id), autenticacao.getName());
        return ManutencaoEpiResponse.fromOutputDTO(manutencao);
    }

    @GetMapping("/{id}/manutencoes")
    @Operation(summary = "Consulta o histórico persistido de manutenção do EPI")
    public List<ManutencaoEpiResponse> listarHistoricoManutencao(@PathVariable Integer id) {
        return manutencaoUseCase.listarHistoricoPorEpi(id).stream()
                .map(ManutencaoEpiResponse::fromOutputDTO)
                .toList();
    }
}
