/*
 * Tratamento de Exception genérica (#249). Este advice é o último consultado (OrdemAdvices.ERRO_INESPERADO):
 * só recebe a exceção que nenhum outro advice tratou, nem por ela nem pela causa.
 *
 * Decisão:
 * - Erro inesperado (NullPointerException, falha de banco etc.) responde 500 no mesmo contrato
 *   {timestamp, status, error, message} dos demais erros, em vez do corpo padrão do Spring em /error. A mensagem
 *   é genérica: o texto da exceção pode trazer detalhes internos (SQL, nomes de classe) e fica só no log.
 * - Exceções que o próprio Spring já traduz para um status (JSON malformado ou de tipo errado, 404 de rota,
 *   405, parâmetro ausente, ResponseStatusException) e as do Spring Security são relançadas. O Spring segue então
 *   o tratamento padrão, e o status e o corpo dessas respostas continuam os de antes.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static br.edu.safeplace.backend.adapters.in.web.CorpoErro.corpo;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(OrdemAdvices.ERRO_INESPERADO)
public class ErroInesperadoExceptionHandler {

    static final String ERRO = "Erro Interno";
    static final String MENSAGEM = "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.";

    private static final Logger LOG = LoggerFactory.getLogger(ErroInesperadoExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> erroInesperado(Exception ex) throws Exception {
        if (traduzidaPeloSpring(ex)) {
            // Relançar a própria exceção devolve o tratamento ao Spring (ExceptionHandlerExceptionResolver).
            throw ex;
        }
        LOG.error("Erro não tratado na API", ex);
        return corpo(HttpStatus.INTERNAL_SERVER_ERROR, ERRO, MENSAGEM);
    }

    static boolean traduzidaPeloSpring(Exception ex) {
        return ex instanceof ErrorResponse
                || ex instanceof TypeMismatchException
                || ex instanceof HttpMessageConversionException
                || ex instanceof BindException
                || ex instanceof AccessDeniedException
                || ex instanceof AuthenticationException
                || AnnotatedElementUtils.hasAnnotation(ex.getClass(), ResponseStatus.class);
    }
}
