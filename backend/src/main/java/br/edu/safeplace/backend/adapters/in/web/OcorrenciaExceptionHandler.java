package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class OcorrenciaExceptionHandler {

    @ExceptionHandler(OcorrenciaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleOcorrenciaNaoEncontrada(OcorrenciaNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Não Encontrado",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleTransicaoStatusInvalida(TransicaoStatusInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Transição de Status Inválida",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ProtocoloCATInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleProtocoloCATInvalido(ProtocoloCATInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Protocolo CAT Inválido",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DataOcorrenciaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleDataOcorrenciaInvalida(DataOcorrenciaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Data de Ocorrência Inválida",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Operação Inválida",
                "message", ex.getMessage()
        ));
    }
}
