package br.edu.safeplace.backend.domain.epi.exception;

/**
 * Exceção lançada quando dados obrigatórios para o cálculo de projeção de desgaste
 * estão ausentes (UC10 Exceção I, RF21). Evita estimativas fictícias e permite
 * apresentar o erro estruturado para nova tentativa após retificação.
 */
public class DadosInsuficientesParaProjecaoException extends RuntimeException {

    private final Integer epiId;

    public DadosInsuficientesParaProjecaoException(Integer epiId, String mensagem) {
        super(mensagem);
        this.epiId = epiId;
    }

    public Integer getEpiId() {
        return epiId;
    }
}
