package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "lotes_epi")
public class LoteEpiEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "numero_lote") private String numeroLote;
    @Column(name = "nota_fiscal") private String notaFiscal;
    @Column(name = "data_fabricacao") private LocalDate dataFabricacao;
    private LocalDate validade;
    @Column(name = "quantidade_recebida", nullable = false)
    private Integer quantidadeRecebida;
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private ModeloEpiEntity modelo;

    protected LoteEpiEntity() { }
    public LoteEpiEntity(Integer id, String numeroLote, String notaFiscal, LocalDate dataFabricacao,
                         LocalDate validade, Integer quantidadeRecebida, ModeloEpiEntity modelo) {
        this.id = id; this.numeroLote = numeroLote; this.notaFiscal = notaFiscal;
        this.dataFabricacao = dataFabricacao; this.validade = validade;
        this.quantidadeRecebida = quantidadeRecebida; this.modelo = modelo;
    }
    public Integer getId() { return id; }
    public String getNumeroLote() { return numeroLote; }
    public String getNotaFiscal() { return notaFiscal; }
    public LocalDate getDataFabricacao() { return dataFabricacao; }
    public LocalDate getValidade() { return validade; }
    public Integer getQuantidadeRecebida() { return quantidadeRecebida; }
    public ModeloEpiEntity getModelo() { return modelo; }
}
