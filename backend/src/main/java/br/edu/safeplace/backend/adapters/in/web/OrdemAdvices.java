package br.edu.safeplace.backend.adapters.in.web;

import org.springframework.core.Ordered;

/**
 * Ordem em que o Spring consulta os advices da API (#249). Para cada exceção, o Spring percorre os advices
 * nessa ordem e usa o primeiro que tiver um handler para ela (ou para a causa dela). Sem {@code @Order}, a
 * ordem dependeria do registro dos beans.
 *
 * <ol>
 *   <li>{@link #COM_ESCOPO}: advices restritos a um controlador, só para corpos com campos extras
 *       ({@code impedimentos} em Tarefa, {@code emprestimoAtivoId} em Empréstimo);</li>
 *   <li>{@link #GLOBAL}: o {@link ApiExceptionHandler}, com um handler por tipo de exceção;</li>
 *   <li>{@link #ERRO_INESPERADO}: o {@link ErroInesperadoExceptionHandler}, que só recebe o que nenhum outro
 *       advice tratou. Ele precisa ser um advice à parte: um {@code @ExceptionHandler(Exception.class)} dentro
 *       do advice global seria escolhido antes de o Spring olhar a causa da exceção, e uma
 *       {@code IllegalArgumentException} embrulhada (por exemplo, lançada por um construtor durante a leitura
 *       do JSON) deixaria de responder 400 "Requisição Inválida".</li>
 * </ol>
 */
final class OrdemAdvices {

    static final int COM_ESCOPO = 0;
    static final int GLOBAL = 1;
    static final int ERRO_INESPERADO = Ordered.LOWEST_PRECEDENCE;

    private OrdemAdvices() {
    }
}
