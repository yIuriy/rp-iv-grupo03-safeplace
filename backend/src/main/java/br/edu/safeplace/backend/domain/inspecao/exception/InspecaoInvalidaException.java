/*
 * Registro de inspeção recusado por dado que a US09 exige (issue #134): data ausente ou futura,
 * nenhum item verificado, item sem referência avaliada ou sem resultado. A API responde 400.
 */
package br.edu.safeplace.backend.domain.inspecao.exception;

public class InspecaoInvalidaException extends RuntimeException {

    public InspecaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
