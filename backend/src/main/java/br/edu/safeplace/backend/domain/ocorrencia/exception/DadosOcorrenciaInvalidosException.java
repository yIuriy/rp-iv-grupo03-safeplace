package br.edu.safeplace.backend.domain.ocorrencia.exception;

/**
 * Relato ou critério de consulta recusado pelo backend (issue #195): campo obrigatório ausente ou
 * valor fora do vocabulário aprovado, como gravidade, causa raiz ou tipo de ocorrência.
 */
public class DadosOcorrenciaInvalidosException extends IllegalArgumentException {
    public DadosOcorrenciaInvalidosException(String mensagem) {
        super(mensagem);
    }
}
