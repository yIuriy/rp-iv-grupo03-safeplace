package br.edu.safeplace.backend.domain.area_risco.exception;

public class AreaRiscoNaoEncontradaException extends RuntimeException {

    public AreaRiscoNaoEncontradaException(Integer id) {
        super("Área de risco não encontrada para o id " + id + ".");
    }

    public AreaRiscoNaoEncontradaException(String codigo) {
        super("Área de risco não encontrada para o código " + codigo + ".");
    }
}
