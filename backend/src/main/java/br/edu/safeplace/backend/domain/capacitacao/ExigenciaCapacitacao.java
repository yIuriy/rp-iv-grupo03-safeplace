package br.edu.safeplace.backend.domain.capacitacao;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Exigência normativa de certificação ou treinamento para uma tarefa ou EPI (RF10, UC02).
 * Entidade JPA da tabela exigencias_capacitacao (#257).
 *
 * <p>O par {@code (alvo, alvoId)} permanece como referência polimórfica conforme a seção 2.2 da
 * decisão #254: pode apontar tanto para {@code Tarefa} quanto para {@code Epi}, sem tabela única
 * de destino no banco (migração V15).</p>
 */
@Entity
@Table(name = "exigencias_capacitacao")
public class ExigenciaCapacitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome_capacitacao", nullable = false, length = 255)
    private String nomeCapacitacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_capacitacao", nullable = false, length = 50)
    private TipoCapacitacao tipoCapacitacao;

    @Column(name = "alvo_id", nullable = false)
    private Integer alvoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alvo", nullable = false, length = 50)
    private TipoAlvo alvo;

    public enum TipoCapacitacao {
        TREINAMENTO,
        CERTIFICACAO
    }

    public enum TipoAlvo {
        TAREFA,
        EPI
    }

    /** Exclusivo do JPA (decisão #254). */
    protected ExigenciaCapacitacao() {
    }

    public ExigenciaCapacitacao(Integer id, String nomeCapacitacao, TipoCapacitacao tipoCapacitacao,
                                Integer alvoId, TipoAlvo alvo) {
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

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ExigenciaCapacitacao exigencia)) {
            return false;
        }
        return getId() != null && getId().equals(exigencia.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
