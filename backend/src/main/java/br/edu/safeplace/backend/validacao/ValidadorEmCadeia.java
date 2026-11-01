package br.edu.safeplace.backend.validacao;

import java.util.List;

/**
 * Base dos validadores da camada: guarda o primeiro elo da cadeia e decide qual exceção lançar.
 * Cada classe validada tem uma subclasse que monta a sua cadeia no construtor.
 *
 * @param <T> tipo validado
 */
public abstract class ValidadorEmCadeia<T> implements Validador<T> {

    private final EloValidacao<T> primeiro;

    protected ValidadorEmCadeia(EloValidacao<T> primeiro) {
        this.primeiro = primeiro;
    }

    @Override
    public ResultadoValidacao verificar(T alvo) {
        ResultadoValidacao resultado = new ResultadoValidacao();
        primeiro.processar(alvo, resultado);
        return resultado;
    }

    /**
     * Lança a exceção própria do primeiro problema, se ele tiver uma. Caso contrário, reúne todas
     * as mensagens na exceção padrão do validador.
     */
    @Override
    public void validar(T alvo) {
        ResultadoValidacao resultado = verificar(alvo);
        if (resultado.valido()) {
            return;
        }
        Problema primeiroProblema = resultado.problemas().getFirst();
        if (primeiroProblema.excecao().isPresent()) {
            throw primeiroProblema.excecao().get().get();
        }
        throw excecaoPadrao(resultado.mensagens());
    }

    /** Exceção usada quando os problemas não têm exceção própria. */
    protected RuntimeException excecaoPadrao(List<String> mensagens) {
        return new IllegalArgumentException(String.join("; ", mensagens));
    }
}
