package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.LoginEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.TokenSaidaDTO;
import br.edu.safeplace.backend.application.port.in.AutenticarUsuarioCasoDeUso;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.usuario.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AutenticacaoControlador.class, UsuarioTratadorExcecoes.class})
@Import(SecurityConfig.class)
class AutenticacaoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticarUsuarioCasoDeUso autenticarUsuarioCasoDeUso;

    @Test
    @DisplayName("Deve retornar 200 OK e token JWT ao logar com credenciais válidas")
    void deveLogarComSucesso() throws Exception {
        TokenSaidaDTO saida = new TokenSaidaDTO(
                "token.jwt.gerado", "Bearer", "gestor@safeplace.com", "Gestor Silva", "GESTOR_SEGURANCA"
        );
        when(autenticarUsuarioCasoDeUso.autenticar(any(LoginEntradaDTO.class))).thenReturn(saida);

        String json = """
                {
                    "email": "gestor@safeplace.com",
                    "senha": "SenhaValida@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.jwt.gerado"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value("gestor@safeplace.com"))
                .andExpect(jsonPath("$.nome").value("Gestor Silva"))
                .andExpect(jsonPath("$.perfil").value("GESTOR_SEGURANCA"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando credenciais forem inválidas")
    void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        when(autenticarUsuarioCasoDeUso.autenticar(any(LoginEntradaDTO.class)))
                .thenThrow(new CredenciaisInvalidasException("Credenciais inválidas."));

        String json = """
                {
                    "email": "gestor@safeplace.com",
                    "senha": "senhaErrada"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Não Autorizado"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando tentativa de login for com perfil Colaborador")
    void deveRetornar401QuandoColaboradorTentarLogin() throws Exception {
        when(autenticarUsuarioCasoDeUso.autenticar(any(LoginEntradaDTO.class)))
                .thenThrow(new CredenciaisInvalidasException("Colaborador não possui permissão de login no sistema."));

        String json = """
                {
                    "email": "colaborador@safeplace.com",
                    "senha": "qualquerSenha"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Colaborador não possui permissão de login no sistema."));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando payload de login estiver com campos em branco")
    void deveRetornar400QuandoCamposEmBranco() throws Exception {
        String json = """
                {
                    "email": "",
                    "senha": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
