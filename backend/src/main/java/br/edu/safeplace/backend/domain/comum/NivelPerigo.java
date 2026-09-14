package br.edu.safeplace.backend.domain.comum;

/**
 * Grau de periculosidade compartilhado pelo núcleo.
 *
 * <p>O diagrama de classes define uma única enumeração {@code NivelPerigo} usada por
 * {@code AreaRisco}, {@code Setor} e {@code Tarefa}. Por isso ela vive em {@code domain.comum},
 * e não dentro de um dos módulos, evitando dependência cruzada entre {@code area_risco} e
 * {@code tarefa}.</p>
 *
 * <p>Os valores seguem o vocabulário de UC03 e do diagrama de classes (baixo, médio, alto e
 * crítico). UC11 e US06 ainda descrevem leve, moderado, grave e crítico; a unificação está
 * registrada na issue #77 e no glossário.</p>
 */
public enum NivelPerigo {
    BAIXO(1),
    MEDIO(2),
    ALTO(3),
    CRITICO(4);

    private final int gravidade;

    NivelPerigo(int gravidade) {
        this.gravidade = gravidade;
    }

    public int getGravidade() {
        return gravidade;
    }

    /**
     * Indica se este nível é igual ou mais grave que o informado.
     */
    public boolean isPeloMenos(NivelPerigo outro) {
        if (outro == null) {
            throw new IllegalArgumentException("Nível de perigo de comparação é obrigatório.");
        }
        return this.gravidade >= outro.gravidade;
    }
}
