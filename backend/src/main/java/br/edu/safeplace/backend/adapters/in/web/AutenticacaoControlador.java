package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.LoginEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.TokenSaidaDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioCasoDeUso;
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
public class AutenticacaoControlador {

    private final AutenticarUsuarioCasoDeUso autenticarUsuarioCasoDeUso;

    public AutenticacaoControlador(AutenticarUsuarioCasoDeUso autenticarUsuarioCasoDeUso) {
        this.autenticarUsuarioCasoDeUso = autenticarUsuarioCasoDeUso;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica Gestor de Segurança ou Supervisor e retorna token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou usuário sem permissão de acesso")
    })
    public ResponseEntity<LoginResposta> login(@Valid @RequestBody LoginRequisicao requisicao) {
        LoginEntradaDTO entrada = new LoginEntradaDTO(requisicao.email(), requisicao.senha());
        TokenSaidaDTO saida = autenticarUsuarioCasoDeUso.autenticar(entrada);

        LoginResposta resposta = new LoginResposta(
                saida.token(),
                saida.tipo(),
                saida.email(),
                saida.nome(),
                saida.perfil()
        );

        return ResponseEntity.ok(resposta);
    }
}
