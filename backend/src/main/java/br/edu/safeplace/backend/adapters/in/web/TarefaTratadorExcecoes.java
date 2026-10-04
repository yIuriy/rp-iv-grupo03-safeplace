package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
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

    @ExceptionHandler(EpiNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleEpiNaoEncontrado(EpiNaoEncontradoException ex) {
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

    /**
     * RF10, UC02 e UC11: bloqueio impeditivo por pendências de certificação ou treinamento.
     */
    @ExceptionHandler(br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> handleAlocacaoBloqueada(
            br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException ex) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Alocação Bloqueada");
        body.put("message", ex.getMessage());
        body.put("impedimentos", ex.getImpedimentos());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Requisição Inválida",
                "message", ex.getMessage()));
    }
}
