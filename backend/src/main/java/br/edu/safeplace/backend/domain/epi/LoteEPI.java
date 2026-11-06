package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Recebimento de um modelo de EPI; entidade JPA da tabela lotes_epi (#258). */
@Entity
@Table(name = "lotes_epi")
public class LoteEPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_lote")
    private String numeroLote;

    @Column(name = "nota_fiscal")
    private String notaFiscal;

    @Column(name = "data_fabricacao")
    private LocalDate dataFabricacao;

    private LocalDate validade;

    @Column(name = "quantidade_recebida", nullable = false)
    private int quantidadeRecebida;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private ModeloEPI modelo;

    /** Exclusivo do JPA (decisão #254). */
    protected LoteEPI() {
    }

    public LoteEPI(
            Integer id,
            String numeroLote,
            String notaFiscal,
            LocalDate dataFabricacao,
            LocalDate validade,
            int quantidadeRecebida,
            ModeloEPI modelo
    ) {

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
