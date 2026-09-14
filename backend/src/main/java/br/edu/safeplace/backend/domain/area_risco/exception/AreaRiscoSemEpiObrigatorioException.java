package br.edu.safeplace.backend.domain.area_risco.exception;

/**
 * UC03, RN1 e cenário de exceção II: bloqueio de cadastro sem vínculo de EPIs obrigatórios.
 */
public class AreaRiscoSemEpiObrigatorioException extends RuntimeException {

    public AreaRiscoSemEpiObrigatorioException(String codigo) {
        super("A área de risco " + codigo
                + " exige ao menos um EPI obrigatório para acesso ao setor.");
    }
}
