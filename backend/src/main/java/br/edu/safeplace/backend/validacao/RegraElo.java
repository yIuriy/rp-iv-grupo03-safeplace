package br.edu.safeplace.backend.validacao;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Elo para regras simples, de uma condição e uma mensagem (por exemplo, campo obrigatório). Evita
 * uma classe por campo; regras com lógica própria continuam como subclasses de {@link EloValidacao}.
 *
 * @param <T> tipo validado
 */
public final class RegraElo<T> extends EloValidacao<T> {

    private final Predicate<T> violada;
    private final Function<T, String> mensagem;
    private final Function<String, RuntimeException> excecao;

    private RegraElo(Predicate<T> violada, Function<T, String> mensagem, Function<String, RuntimeException> excecao) {
        this.violada = violada;
        this.mensagem = mensagem;
        this.excecao = excecao;
    }

    /** Regra que, violada, lança {@link IllegalArgumentException} com a mensagem (resposta 400). */
    public static <T> RegraElo<T> exigir(Predicate<T> violada, String mensagem) {
        return new RegraElo<>(violada, alvo -> mensagem, IllegalArgumentException::new);
    }

    /** Regra com exceção própria e mensagem fixa. */
    public static <T> RegraElo<T> exigir(Predicate<T> violada, String mensagem,
                                         Function<String, RuntimeException> excecao) {
        return new RegraElo<>(violada, alvo -> mensagem, excecao);
    }

    /** Regra com exceção própria e mensagem que depende do objeto validado. */
    public static <T> RegraElo<T> exigirComMensagem(Predicate<T> violada, Function<T, String> mensagem,
                                                    Function<String, RuntimeException> excecao) {
        return new RegraElo<>(violada, mensagem, excecao);
    }

    /** Verdadeiro para texto nulo ou só com espaços. */
    public static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    @Override
    protected void verificar(T alvo, ResultadoValidacao resultado) {
        if (violada.test(alvo)) {
            String texto = mensagem.apply(alvo);
            resultado.adicionar(Problema.comExcecao(texto, () -> excecao.apply(texto)));
        }
    }
}
