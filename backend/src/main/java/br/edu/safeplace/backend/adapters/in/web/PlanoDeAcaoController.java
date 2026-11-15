package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.port.in.GerenciarPlanoDeAcaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Planos de Ação", description = "Endpoints para gerenciamento de planos de ação e acompanhamento de ações vinculadas a ocorrências")
public class PlanoDeAcaoController {

    private final GerenciarPlanoDeAcaoUseCase useCase;

    public PlanoDeAcaoController(GerenciarPlanoDeAcaoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/api/ocorrencias/{idOcorrencia}/planos-acao")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma nova ação no plano vinculado à ocorrência")
    public PlanoDeAcaoResponse cadastrarAcao(
            @PathVariable Integer idOcorrencia,
            @Valid @RequestBody CriarPlanoDeAcaoRequest request) {
        return PlanoDeAcaoResponse.fromOutputDTO(useCase.cadastrar(idOcorrencia, request.toInputDTO()));
    }

    @GetMapping("/api/ocorrencias/{idOcorrencia}/planos-acao")
    @Operation(summary = "Lista todas as ações do plano vinculadas a uma ocorrência")
    public List<PlanoDeAcaoResponse> listarPorOcorrencia(@PathVariable Integer idOcorrencia) {
        return useCase.listarPorOcorrencia(idOcorrencia).stream()
                .map(PlanoDeAcaoResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/api/planos-acao/{id}")
    @Operation(summary = "Busca detalhes de uma ação pelo ID")
    public PlanoDeAcaoResponse buscarPorId(@PathVariable Integer id) {
        return PlanoDeAcaoResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @PatchMapping("/api/planos-acao/{id}/status")
    @Operation(summary = "Conclui uma ação aberta (única transição aceita: ABERTA para CONCLUIDA)")
    public PlanoDeAcaoResponse atualizarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarStatusPlanoRequest request) {
        return PlanoDeAcaoResponse.fromOutputDTO(useCase.atualizarStatus(id, request.toInputDTO()));
    }

    @PostMapping("/api/planos-acao/{id}/prorrogacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma prorrogação de prazo com justificativa técnica")
    public ProrrogacaoPrazoResponse prorrogarPrazo(
            @PathVariable Integer id,
            @Valid @RequestBody ProrrogarPrazoRequest request) {
        return ProrrogacaoPrazoResponse.fromOutputDTO(useCase.prorrogarPrazo(id, request.toInputDTO()));
    }

    @GetMapping("/api/planos-acao/{id}/prorrogacoes")
    @Operation(summary = "Lista o histórico de prorrogações de prazo de uma ação")
    public List<ProrrogacaoPrazoResponse> listarProrrogacoes(@PathVariable Integer id) {
        return useCase.listarProrrogacoes(id).stream()
                .map(ProrrogacaoPrazoResponse::fromOutputDTO)
                .toList();
    }

    @PatchMapping("/api/planos-acao/{id}/responsavel")
    @Operation(summary = "Reatribui o responsável pela ação (colaborador ou setor)")
    public PlanoDeAcaoResponse atualizarResponsavel(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizarResponsavelRequest request) {
        return PlanoDeAcaoResponse.fromOutputDTO(useCase.atualizarResponsavel(id, request.toInputDTO()));
    }

    @PatchMapping("/api/planos-acao/{id}/alerta")
    @Operation(summary = "Configura o alerta de prazo da ação")
    public PlanoDeAcaoResponse configurarAlerta(
            @PathVariable Integer id,
            @Valid @RequestBody ConfigurarAlertaRequest request) {
        return PlanoDeAcaoResponse.fromOutputDTO(useCase.configurarAlerta(id, request.toInputDTO()));
    }
}
