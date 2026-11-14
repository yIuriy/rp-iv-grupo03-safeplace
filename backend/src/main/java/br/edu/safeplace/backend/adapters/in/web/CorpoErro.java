package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Fábrica do corpo de erro da API, no contrato {timestamp, status, error, message} que o frontend e os testes
 * já leem. O mapa é mutável para que um tratador acrescente campos (ex.: impedimentos da alocação).
 *
 * <p>A mensagem pode ser nula: uma exceção lançada sem mensagem chega aqui com {@code getMessage() == null}.
 * O {@link LinkedHashMap} aceita o valor nulo (o {@code Map.of} lançava {@code NullPointerException} e a
 * resposta virava 500), então a resposta mantém o status do tratador e o JSON traz {@code "message": null}
 * (#249). O frontend já trata esse caso e mostra o texto padrão da tela.
 */
public final class CorpoErro {

    private CorpoErro() {
    }

    /** O corpo de erro; usado também fora do MVC, pelos tratadores de segurança (401 e 403). */
    public static Map<String, Object> mapa(HttpStatus status, String erro, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", erro);
        body.put("message", mensagem);
        return body;
    }

    static ResponseEntity<Map<String, Object>> corpo(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(mapa(status, erro, mensagem));
    }
}
