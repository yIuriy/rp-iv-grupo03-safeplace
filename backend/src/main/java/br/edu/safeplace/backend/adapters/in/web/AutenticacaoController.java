package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.LoginInputDTO;
import br.edu.safeplace.backend.application.dto.output.TokenOutputDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e obtenção de token JWT")
public class AutenticacaoController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public AutenticacaoController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica Gestor de Segurança ou Supervisor e retorna token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou usuário sem permissão de acesso")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest requisicao) {
        LoginInputDTO entrada = new LoginInputDTO(requisicao.email(), requisicao.senha());
        TokenOutputDTO saida = autenticarUsuarioUseCase.autenticar(entrada);

        LoginResponse resposta = new LoginResponse(
                saida.token(),
                saida.tipo(),
                saida.email(),
                saida.nome(),
                saida.perfil()
        );

        return ResponseEntity.ok(resposta);
    }
}
