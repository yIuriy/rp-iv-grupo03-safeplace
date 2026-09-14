package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;

@RestControllerAdvice
public class AreaRiscoTratadorExcecoes {

    @ExceptionHandler(AreaRiscoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleAreaRiscoNaoEncontrada(AreaRiscoNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Não Encontrado",
                "message", ex.getMessage()));
    }

    /**
     * UC03, cenário de exceção I: código de setor já em uso.
     */
    @ExceptionHandler(CodigoAreaRiscoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleCodigoDuplicado(CodigoAreaRiscoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Código Já Em Uso",
                "message", ex.getMessage()));
    }

    /**
     * UC03, cenário de exceção II: bloqueio de cadastro sem vínculo de EPIs obrigatórios.
     */
    @ExceptionHandler(AreaRiscoSemEpiObrigatorioException.class)
    public ResponseEntity<Map<String, Object>> handleSemEpiObrigatorio(AreaRiscoSemEpiObrigatorioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "EPI Obrigatório Ausente",
                "message", ex.getMessage()));
    }
}
