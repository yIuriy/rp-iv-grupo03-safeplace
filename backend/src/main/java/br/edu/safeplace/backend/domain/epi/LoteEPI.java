package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

public class LoteEPI {

    private final Integer id;
    private final String numeroLote;
    private final String notaFiscal;
    private final LocalDate dataFabricacao;
    private final LocalDate validade;
    private final int quantidadeRecebida;
    private final ModeloEPI modelo;

    public LoteEPI(
            Integer id,
            String numeroLote,
            String notaFiscal,
            LocalDate dataFabricacao,
            LocalDate validade,
            int quantidadeRecebida,
            ModeloEPI modelo
    ) {
        if (modelo == null) {
            throw new IllegalArgumentException(
                    "Modelo do EPI é obrigatório para o lote."
            );
        }

        this.id = id;
        this.numeroLote = numeroLote;
        this.notaFiscal = notaFiscal;
        this.dataFabricacao = dataFabricacao;
        this.validade = validade;
        this.quantidadeRecebida = quantidadeRecebida;
        this.modelo = modelo;
    }

    public LoteEPI(String numeroLote, String notaFiscal, LocalDate dataFabricacao,
                   LocalDate validade, int quantidadeRecebida, ModeloEPI modelo) {
        this(null, numeroLote, notaFiscal, dataFabricacao, validade, quantidadeRecebida, modelo);
    }

    public Integer getId() {
        return id;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public String getNotaFiscal() {
        return notaFiscal;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public int getQuantidadeRecebida() {
        return quantidadeRecebida;
    }

    public ModeloEPI getModelo() {
        return modelo;
    }
}
