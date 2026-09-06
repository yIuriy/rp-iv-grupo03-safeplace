package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDateTime;

public class MovimentacaoEstoque {
    private final Integer id;
    private final Integer epiId;
    private final TipoMovimentacao tipo;
    private final int quantidade;
    private final LocalDateTime dataHora;
    private final String motivo;

    public MovimentacaoEstoque(Integer id, Integer epiId, TipoMovimentacao tipo, int quantidade,
                               LocalDateTime dataHora, String motivo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade movimentada deve ser maior que zero.");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException("Data e hora da movimentação são obrigatórias.");
        }

        this.id = id;
        this.epiId = epiId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
        this.motivo = motivo;
    }

    public Integer getId() {
        return id;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getMotivo() {
        return motivo;
    }
}
