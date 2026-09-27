package br.edu.safeplace.backend.domain.ocorrencia.exception;

/**
 * Pendência da regra 1 de UC07: ocorrência grave sem ao menos uma ação preventiva no plano.
 */
public class AcaoPreventivaObrigatoriaException extends RuntimeException {
    public AcaoPreventivaObrigatoriaException(Integer idOcorrencia) {
        super(String.format(
                "A ocorrência %d é grave e exige ao menos uma ação preventiva no plano de ação.", idOcorrencia));
    }
}
