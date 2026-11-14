package br.edu.safeplace.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.adapters.in.web.CorpoErro;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Respostas 401 e 403 do Spring Security, que acontecem antes do controlador e por isso não passam pelos
 * advices. O corpo vem da mesma fábrica dos advices ({@link CorpoErro}) e é escrito com o Jackson 3, o mesmo
 * do MVC, para que as datas saiam no mesmo formato (#249). Antes, este tratador incluía o campo {@code path} e
 * escrevia o {@code timestamp} com {@code toString()}, fora do contrato {timestamp, status, error, message}.
 */
@Component
public class RestSegurancaExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escrever(response, HttpStatus.UNAUTHORIZED, "Não Autorizado",
                "Acesso não autenticado. Forneça um token JWT válido.");
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escrever(response, HttpStatus.FORBIDDEN, "Acesso Proibido",
                "Seu perfil não possui permissão para acessar este recurso.");
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String erro, String mensagem)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status.value());
        response.getWriter().write(jsonMapper.writeValueAsString(CorpoErro.mapa(status, erro, mensagem)));
    }
}
