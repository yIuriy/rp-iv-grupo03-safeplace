package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "epi_id", nullable = false)
    private Integer epiId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    public MovimentacaoEstoqueEntity() {
    }

    public MovimentacaoEstoqueEntity(Integer id, Integer epiId, String tipo, Integer quantidade,
                                     LocalDateTime dataHora, String motivo) {
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

    public String getTipo() {
        return tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getMotivo() {
        return motivo;
    }
}
