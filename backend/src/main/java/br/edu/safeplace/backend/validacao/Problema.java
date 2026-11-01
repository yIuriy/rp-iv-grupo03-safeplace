package br.edu.safeplace.backend.validacao;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Um problema encontrado por um elo da cadeia de validação.
 *
 * @param mensagem texto exibido quando os problemas são listados (por exemplo, na consulta de aptidão)
 * @param excecao exceção específica a lançar quando o problema bloqueia a operação; vazia quando o
 *                validador usa a sua exceção padrão para reunir os problemas
 */
public record Problema(String mensagem, Optional<Supplier<? extends RuntimeException>> excecao) {

    /** Problema reunido com os demais na exceção padrão do validador. */
    public static Problema de(String mensagem) {
        return new Problema(mensagem, Optional.empty());
    }

    /** Problema com exceção própria, lançada no lugar da exceção padrão do validador. */
    public static Problema comExcecao(String mensagem, Supplier<? extends RuntimeException> excecao) {
        return new Problema(mensagem, Optional.of(excecao));
    }
}
