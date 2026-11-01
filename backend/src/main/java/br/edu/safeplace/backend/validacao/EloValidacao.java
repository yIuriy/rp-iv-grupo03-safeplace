package br.edu.safeplace.backend.validacao;

/**
 * Handler do padrão Chain of Responsibility (GoF): cada elo verifica uma regra e passa o objeto
 * ao próximo elo, até o fim da cadeia ou até um elo interromper.
 *
 * @param <T> tipo validado
 */
public abstract class EloValidacao<T> {

    private EloValidacao<T> proximo;

    /**
     * Liga o próximo elo e o devolve, para montar a cadeia em sequência:
     * {@code primeiro.encadear(segundo).encadear(terceiro)}.
     */
    public EloValidacao<T> encadear(EloValidacao<T> proximo) {
        this.proximo = proximo;
        return proximo;
    }

    /** Verifica a regra deste elo e, se a cadeia não foi interrompida, repassa ao próximo. */
    public final void processar(T alvo, ResultadoValidacao resultado) {
        verificar(alvo, resultado);
        if (!resultado.interrompido() && proximo != null) {
            proximo.processar(alvo, resultado);
        }
    }

    /** A regra deste elo. Registra no resultado os problemas encontrados. */
    protected abstract void verificar(T alvo, ResultadoValidacao resultado);
}
