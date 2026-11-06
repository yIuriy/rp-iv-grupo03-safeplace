package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entrada ou saída de estoque de um EPI (UC06); entidade JPA da tabela movimentacoes_estoque (#258). EPI,
 * lote e responsável são associações por objeto; os getters de id servem aos DTOs, que continuam expondo ids.
 */
@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "epi_id", nullable = false)
    private Epi epi;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private LoteEPI lote;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Colaborador responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    /** Exclusivo do JPA (decisão #254). */
    protected MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(Integer id, Epi epi, LoteEPI lote, Colaborador responsavel,
            TipoMovimentacao tipo, int quantidade, LocalDateTime dataHora, String motivo) {
        this.id = id;
        this.epi = epi;
        this.lote = lote;
        this.responsavel = responsavel;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataHora = dataHora;
        this.motivo = motivo;
    }

    public Integer getId() {
        return id;
    }

    public Epi getEpi() {
        return epi;
    }

    public LoteEPI getLote() {
        return lote;
    }

    public Colaborador getResponsavel() {
        return responsavel;
    }

    public Integer getEpiId() {
        return epi != null ? epi.getId() : null;
    }

    public Integer getLoteId() {
        return lote != null ? lote.getId() : null;
    }

    public Integer getResponsavelId() {
        return responsavel != null ? responsavel.getId() : null;
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
