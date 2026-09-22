/*
 * Tradução das exceções de empréstimo para HTTP, no mesmo formato de corpo dos demais tratadores:
 * 404 empréstimo inexistente, 409 duplicidade pendente de confirmação ou empréstimo já encerrado,
 * 422 entrega impedida por regra do UC12 (saldo, CA, situação, capacitação, colaborador).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoNaoEncontradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;

@RestControllerAdvice(assignableTypes = EmprestimoController.class)
public class EmprestimoExceptionHandler {

    @ExceptionHandler(EmprestimoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(EmprestimoNaoEncontradoException ex) {
        return corpo(HttpStatus.NOT_FOUND, "Não Encontrado", ex.getMessage());
    }

    @ExceptionHandler(EntregaImpedidaException.class)
    public ResponseEntity<Map<String, Object>> entregaImpedida(EntregaImpedidaException ex) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "Entrega Impedida", ex.getMessage());
    }

    @ExceptionHandler(DuplicidadeDePosseException.class)
    public ResponseEntity<Map<String, Object>> duplicidade(DuplicidadeDePosseException ex) {
        ResponseEntity<Map<String, Object>> resposta = corpo(HttpStatus.CONFLICT, "Duplicidade de Posse", ex.getMessage());
        resposta.getBody().put("emprestimoAtivoId", ex.getEmprestimoAtivoId());
        return resposta;
    }

    @ExceptionHandler(EmprestimoJaEncerradoException.class)
    public ResponseEntity<Map<String, Object>> jaEncerrado(EmprestimoJaEncerradoException ex) {
        return corpo(HttpStatus.CONFLICT, "Empréstimo Encerrado", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> dadosInvalidos(IllegalArgumentException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Requisição Inválida", ex.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> corpo(HttpStatus status, String erro, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", erro);
        body.put("message", mensagem);
        return ResponseEntity.status(status).body(body);
    }
}
