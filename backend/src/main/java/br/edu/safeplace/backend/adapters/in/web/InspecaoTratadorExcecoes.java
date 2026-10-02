/*
 * Tratamento de erros do controlador de inspeções (issue #134): registro que a US09 recusa responde
 * 400 com a mensagem do domínio. Área inexistente segue o tratador de áreas de risco (404).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;

@RestControllerAdvice(assignableTypes = InspecaoController.class)
public class InspecaoTratadorExcecoes {

    @ExceptionHandler(InspecaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleInspecaoInvalida(InspecaoInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Inspeção Inválida",
                "message", ex.getMessage()));
    }
}
