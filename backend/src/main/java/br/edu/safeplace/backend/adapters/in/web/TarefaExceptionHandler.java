/*
 * Advice com escopo da tarefa (#249): só a alocação bloqueada (422), cujo corpo leva o campo extra
 * impedimentos com as pendências de capacitação. A exceção só sai da alocação (POST /api/tarefas/{id}/alocacoes);
 * a consulta de aptidão devolve os impedimentos num 200, sem exceção.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static br.edu.safeplace.backend.adapters.in.web.CorpoErro.corpo;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;

@RestControllerAdvice(assignableTypes = TarefaController.class)
@Order(OrdemAdvices.COM_ESCOPO)
public class TarefaExceptionHandler {

    @ExceptionHandler(AlocacaoBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> alocacaoBloqueada(AlocacaoBloqueadaException ex) {
        ResponseEntity<Map<String, Object>> resposta =
                corpo(HttpStatus.UNPROCESSABLE_ENTITY, "Alocação Bloqueada", ex.getMessage());
        resposta.getBody().put("impedimentos", ex.getImpedimentos());
        return resposta;
    }
}
