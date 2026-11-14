/*
 * Advice com escopo do empréstimo (#249): só a duplicidade de posse (409), cujo corpo leva o campo extra
 * emprestimoAtivoId para o frontend pedir a confirmação da troca. As demais exceções de empréstimo ficam no
 * ApiExceptionHandler.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static br.edu.safeplace.backend.adapters.in.web.CorpoErro.corpo;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;

@RestControllerAdvice(assignableTypes = EmprestimoController.class)
@Order(OrdemAdvices.COM_ESCOPO)
public class EmprestimoExceptionHandler {

    @ExceptionHandler(DuplicidadeDePosseException.class)
    public ResponseEntity<Map<String, Object>> duplicidade(DuplicidadeDePosseException ex) {
        ResponseEntity<Map<String, Object>> resposta = corpo(HttpStatus.CONFLICT, "Duplicidade de Posse", ex.getMessage());
        resposta.getBody().put("emprestimoAtivoId", ex.getEmprestimoAtivoId());
        return resposta;
    }
}
