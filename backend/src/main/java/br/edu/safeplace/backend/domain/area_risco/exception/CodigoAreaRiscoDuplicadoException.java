package br.edu.safeplace.backend.domain.area_risco.exception;

/**
 * UC03, cenário de exceção I: setor com código duplicado.
 */
public class CodigoAreaRiscoDuplicadoException extends RuntimeException {

    public CodigoAreaRiscoDuplicadoException(String codigo) {
        super("Já existe uma área de risco cadastrada com o código " + codigo + ".");
    }
}
