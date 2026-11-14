/*
 * Corpo das respostas 401 e 403 do Spring Security (#249): mesmo contrato {timestamp, status, error, message}
 * dos advices, sem o campo path, com o timestamp no formato ISO do MVC e acentos em UTF-8.
 */
package br.edu.safeplace.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import tools.jackson.databind.json.JsonMapper;

class RestSegurancaExceptionHandlerTest {

    private final RestSegurancaExceptionHandler handler = new RestSegurancaExceptionHandler();
    private final JsonMapper leitor = JsonMapper.builder().build();

    @Test
    void naoAutenticadoRespondeNoContratoDaApi() throws Exception {
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        handler.commence(new MockHttpServletRequest("GET", "/api/epis"), resposta,
                new InsufficientAuthenticationException("sem token"));

        assertThat(resposta.getStatus()).isEqualTo(401);
        assertThat(resposta.getContentType()).startsWith("application/json");
        Map<String, Object> corpo = ler(resposta);
        assertThat(corpo).containsOnlyKeys("timestamp", "status", "error", "message")
                .containsEntry("status", 401)
                .containsEntry("error", "Não Autorizado")
                .containsEntry("message", "Acesso não autenticado. Forneça um token JWT válido.");
        assertThat(LocalDateTime.parse((String) corpo.get("timestamp"))).isNotNull();
    }

    @Test
    void acessoNegadoRespondeNoContratoDaApi() throws Exception {
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest("POST", "/api/usuarios"), resposta, new AccessDeniedException("perfil"));

        assertThat(resposta.getStatus()).isEqualTo(403);
        assertThat(ler(resposta)).containsOnlyKeys("timestamp", "status", "error", "message")
                .containsEntry("status", 403)
                .containsEntry("error", "Acesso Proibido")
                .containsEntry("message", "Seu perfil não possui permissão para acessar este recurso.");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ler(MockHttpServletResponse resposta) throws Exception {
        assertThat(resposta.getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");
        return leitor.readValue(resposta.getContentAsByteArray(), Map.class);
    }
}
