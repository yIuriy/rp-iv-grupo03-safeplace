package br.edu.safeplace.backend.domain.capacitacao;

public class ExigenciaCapacitacao {
    private final Integer id;
    private final String nomeCapacitacao;
    private final TipoCapacitacao tipoCapacitacao;
    private final Integer alvoId;
    private final TipoAlvo alvo;

    public enum TipoCapacitacao {
        TREINAMENTO,
        CERTIFICACAO
    }

    public enum TipoAlvo {
        TAREFA,
        EPI
    }

    public ExigenciaCapacitacao(Integer id, String nomeCapacitacao, TipoCapacitacao tipoCapacitacao, Integer alvoId, TipoAlvo alvo) {
        this.id = id;
        this.nomeCapacitacao = nomeCapacitacao != null ? nomeCapacitacao.trim() : null;
        this.tipoCapacitacao = tipoCapacitacao;
        this.alvoId = alvoId;
        this.alvo = alvo;
    }

    public Integer getId() {
        return id;
    }

    public String getNomeCapacitacao() {
        return nomeCapacitacao;
    }

    public TipoCapacitacao getTipoCapacitacao() {
        return tipoCapacitacao;
    }

    public Integer getAlvoId() {
        return alvoId;
    }

    public TipoAlvo getAlvo() {
        return alvo;
    }
}
