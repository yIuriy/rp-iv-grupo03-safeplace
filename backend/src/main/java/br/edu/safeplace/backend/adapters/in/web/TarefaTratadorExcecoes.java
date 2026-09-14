package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;

@RestControllerAdvice
public class TarefaTratadorExcecoes {

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleTarefaNaoEncontrada(TarefaNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Não Encontrado",
                "message", ex.getMessage()));
    }

    /**
     * UC11, cenário de exceção I: função sem grau de risco cadastrado bloqueia a alocação.
     */
    @ExceptionHandler(TarefaSemClassificacaoException.class)
    public ResponseEntity<Map<String, Object>> handleTarefaSemClassificacao(TarefaSemClassificacaoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Tarefa Sem Classificação",
                "message", ex.getMessage()));
    }
}
