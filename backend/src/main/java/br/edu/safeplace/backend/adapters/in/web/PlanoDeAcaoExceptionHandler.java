package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPlanoNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class PlanoDeAcaoExceptionHandler {

    @ExceptionHandler(AcaoPlanoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleAcaoNaoEncontrada(AcaoPlanoNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Não Encontrado",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(AcaoPreventivaObrigatoriaException.class)
    public ResponseEntity<Map<String, Object>> handleAcaoPreventivaObrigatoria(AcaoPreventivaObrigatoriaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Ação Preventiva Obrigatória",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PlanoDeAcaoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handlePlanoDeAcaoInvalido(PlanoDeAcaoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Plano de Ação Inválido",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PrazoAcaoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handlePrazoAcaoInvalido(PrazoAcaoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Prazo Inválido",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Requisição Inválida",
                "message", ex.getMessage()
        ));
    }
}
