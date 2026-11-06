package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Fábrica do corpo de erro da API, no contrato {timestamp, status, error, message} que o frontend e os testes
 * já leem. O mapa é mutável para que um tratador acrescente campos (ex.: impedimentos da alocação).
 */
final class CorpoErro {

    private CorpoErro() {
    }

    static ResponseEntity<Map<String, Object>> corpo(HttpStatus status, String erro, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", erro);
        body.put("message", mensagem);
        return ResponseEntity.status(status).body(body);
    }
}
