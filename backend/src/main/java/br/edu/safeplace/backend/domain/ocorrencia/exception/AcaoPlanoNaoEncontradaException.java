package br.edu.safeplace.backend.domain.ocorrencia.exception;

/** Ação do plano de ação inexistente (UC07); a API responde 404 (issue #196). */
public class AcaoPlanoNaoEncontradaException extends RuntimeException {
    public AcaoPlanoNaoEncontradaException(Integer idAcao) {
        super("Ação do plano não encontrada para o ID: " + idAcao);
    }
}
