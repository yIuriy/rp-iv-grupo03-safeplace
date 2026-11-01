/*
 * Contrato da camada de validação (issue #228). Os casos de uso dependem desta interface e chamam
 * validar antes do repository, conforme a avaliação do professor.
 */
package br.edu.safeplace.backend.validacao;

/**
 * Valida um objeto antes de ele seguir para o repository.
 *
 * @param <T> tipo validado: uma entidade de domínio ou um objeto de contexto
 */
public interface Validador<T> {

    /** Executa todas as regras e devolve os problemas encontrados, sem lançar exceção. */
    ResultadoValidacao verificar(T alvo);

    /** Executa as regras e lança a exceção correspondente ao primeiro problema, se houver. */
    void validar(T alvo);
}
