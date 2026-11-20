package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDateTime;
import java.util.Objects;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Registro de manutenção de um EPI (RF04, UC01); entidade JPA da tabela manutencoes_epi (#259).
 * EPI e responsável são associações por objeto; {@link #getEpiId()} e {@link #getResponsavelId()}
 * servem aos DTOs, que continuam expondo ids.
 */
@Entity
@Table(name = "manutencoes_epi")
public class ManutencaoEpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "epi_id", nullable = false)
    private Epi epi;

    @Column(name = "data_manutencao", nullable = false)
    private LocalDateTime dataManutencao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_manutencao", length = 30)
    private TipoManutencao tipoManutencao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoManutencao resultado;

    @Column(name = "responsavel_manutencao", nullable = false, length = 160)
    private String responsavelManutencao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Colaborador responsavel;

    /** Exclusivo do JPA (decisão #254). */
    protected ManutencaoEpi() {
    }

    private ManutencaoEpi(Builder builder) {
        // Os dados obrigatórios são validados por ManutencaoEpiValidador antes do repository (#264).
        this.epi = builder.epi;
        this.dataManutencao = builder.dataManutencao;
        this.tipoManutencao = builder.tipoManutencao;
        this.descricao = builder.descricao;
        this.resultado = builder.resultado;
        this.responsavelManutencao = builder.responsavelManutencao;
        this.responsavel = builder.responsavel;
    }

    public Integer getId() {
        return id;
    }

    public Epi getEpi() {
        return epi;
    }

    public Integer getEpiId() {
        return epi != null ? epi.getId() : null;
    }

    public LocalDateTime getDataManutencao() {
        return dataManutencao;
    }

    public TipoManutencao getTipoManutencao() {
        return tipoManutencao;
    }

    public String getDescricao() {
        return descricao;
    }

    public ResultadoManutencao getResultado() {
        return resultado;
    }

    public String getResponsavelManutencao() {
        return responsavelManutencao;
    }

    public Colaborador getResponsavel() {
        return responsavel;
    }

    public Integer getResponsavelId() {
        return responsavel != null ? responsavel.getId() : null;
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ManutencaoEpi manutencao)) {
            return false;
        }
        return getId() != null && getId().equals(manutencao.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Builder (GoF, #259) para construir registros de manutenção sem parâmetros posicionais nem métodos
     * estáticos de criação. Não executa validação; esta roda em {@code ManutencaoEpiValidador}.
     */
    public static final class Builder {
        private Epi epi;
        private LocalDateTime dataManutencao;
        private TipoManutencao tipoManutencao;
        private String descricao;
        private ResultadoManutencao resultado;
        private String responsavelManutencao;
        private Colaborador responsavel;

        public Builder epi(Epi epi) {
            this.epi = epi;
            return this;
        }

        public Builder realizadaEm(LocalDateTime dataManutencao) {
            this.dataManutencao = dataManutencao;
            return this;
        }

        public Builder tipo(TipoManutencao tipoManutencao) {
            this.tipoManutencao = tipoManutencao;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder resultado(ResultadoManutencao resultado) {
            this.resultado = resultado;
            return this;
        }

        public Builder responsavelManutencao(String responsavelManutencao) {
            this.responsavelManutencao = responsavelManutencao;
            return this;
        }

        public Builder responsavel(Colaborador responsavel) {
            this.responsavel = responsavel;
            return this;
        }

        public ManutencaoEpi build() {
            return new ManutencaoEpi(this);
        }
    }
}
