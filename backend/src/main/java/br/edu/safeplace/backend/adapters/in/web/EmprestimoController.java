/*
 * Controlador REST de empréstimos de EPI (RF11, UC12, US11, issue #106) em /api/emprestimos.
 * Supervisor e Gestor de Segurança operam; o Colaborador beneficiário não acessa o sistema. A
 * autoria vem do token autenticado, nunca do corpo da requisição.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase.Situacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emprestimos")
@Tag(name = "Empréstimos", description = "Entrega, posse e devolução de EPIs a colaboradores (UC12)")
public class EmprestimoController {
    private final ControlarEmprestimoUseCase useCase;

    public EmprestimoController(ControlarEmprestimoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra a entrega de um EPI a um colaborador, com termo de cautela e débito de uma unidade do estoque",
            description = "Valida saldo, CA, duplicidade de posse ativa e pendências de capacitação no servidor antes de confirmar.")
    public EmprestimoResponse registrarEntrega(@Valid @RequestBody RegistrarEntregaRequest request,
                                               Authentication autenticacao) {
        return EmprestimoResponse.fromOutputDTO(useCase.registrarEntrega(request.toInputDTO(), autenticacao.getName()));
    }

    @PostMapping("/{id}/devolucao")
    @Operation(summary = "Registra a devolução do EPI com estado apto, manutenção ou descarte, refletindo destino aprovado e saldo persistido",
            description = "Itens aptos retornam ao saldo de estoque; itens em manutenção ou descarte não creditam saldo.")
    public DevolucaoResponse registrarDevolucao(@PathVariable Integer id,
                                                @Valid @RequestBody RegistrarDevolucaoRequest request,
                                                Authentication autenticacao) {
        return DevolucaoResponse.fromOutputDTO(
                useCase.registrarDevolucao(id, request.toInputDTO(), autenticacao.getName()));
    }

    @GetMapping
    @Operation(summary = "Consulta empréstimos e posse de EPIs conforme UC12",
            description = "Filtra por situacao=PENDENTES ou ATRASADOS e colaboradorId. Para consultar a posse ativa do colaborador, utilize colaboradorId e situacao=PENDENTES.")
    public List<EmprestimoResponse> listar(@RequestParam(required = false) Situacao situacao,
                                           @RequestParam(required = false) Integer colaboradorId) {
        return useCase.listar(situacao, colaboradorId).stream()
                .map(EmprestimoResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta os detalhes de um empréstimo por identificador")
    public EmprestimoResponse buscarPorId(@PathVariable Integer id) {
        return EmprestimoResponse.fromOutputDTO(useCase.buscarPorId(id));
    }

    @GetMapping("/epis-disponiveis")
    @Operation(summary = "Lista os EPIs disponíveis para empréstimo (passo 4 do UC12)")
    public List<EpiResponse> listarEpisDisponiveis() {
        return useCase.listarEpisDisponiveis().stream()
                .map(EpiResponse::fromOutputDTO)
                .toList();
    }
}
