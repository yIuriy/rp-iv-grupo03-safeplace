/*
 * Endpoints REST de cadastro de certificações, treinamentos e exigências de capacitação (RF10,
 * UC02, issue #192). Escrita exclusiva do Gestor de Segurança; a consulta fica em
 * CapacitacaoController.
 */
package br.edu.safeplace.backend.adapters.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ExigenciaCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.CadastrarCapacitacaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/capacitacoes")
@Tag(name = "Capacitações", description = "Cadastro de certificações, treinamentos e exigências (RF10, UC02)")
public class CapacitacaoCadastroController {

    private final CadastrarCapacitacaoUseCase cadastrarUseCase;

    public CapacitacaoCadastroController(CadastrarCapacitacaoUseCase cadastrarUseCase) {
        this.cadastrarUseCase = cadastrarUseCase;
    }

    @PostMapping("/certificacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma certificação de um colaborador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Certificação cadastrada e auditada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, como vencimento no passado ou emissão posterior ao vencimento"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public CertificacaoOutputDTO cadastrarCertificacao(@Valid @RequestBody CadastrarCertificacaoInputDTO entrada) {
        return cadastrarUseCase.cadastrar(entrada);
    }

    @PostMapping("/treinamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um treinamento realizado por um ou mais colaboradores",
            description = "A validade em meses é opcional: sem ela o treinamento não vence.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Treinamento cadastrado e auditado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, como realização no futuro"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public TreinamentoOutputDTO cadastrarTreinamento(@Valid @RequestBody CadastrarTreinamentoInputDTO entrada) {
        return cadastrarUseCase.cadastrar(entrada);
    }

    @PostMapping("/exigencias")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Define a certificação ou o treinamento exigido para uma tarefa ou EPI")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exigência definida e auditada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public ExigenciaCapacitacaoOutputDTO definirExigencia(@Valid @RequestBody DefinirExigenciaInputDTO entrada) {
        return cadastrarUseCase.cadastrar(entrada);
    }
}
