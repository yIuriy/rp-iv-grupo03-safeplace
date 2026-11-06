package br.edu.safeplace.backend.domain.ocorrencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Entidade JPA que mantém o último número sequencial emitido para o par ano/mês do protocolo CAT (#261).
 * Não tem setters públicos: a concorrência é serializada por @Lock(PESSIMISTIC_WRITE) na linha do mês.
 */
@Entity
@Table(name = "sequencias_protocolo_cat")
@IdClass(SequenciaProtocoloCatId.class)
public class SequenciaProtocoloCat {

    @Id
    @Column(nullable = false)
    private Short ano;

    @Id
    @Column(nullable = false)
    private Short mes;

    @Column(name = "ultimo_sequencial", nullable = false)
    private Long ultimoSequencial;

    /** Exclusivo do JPA (decisão #254). */
    protected SequenciaProtocoloCat() {
    }

    public SequenciaProtocoloCat(int ano, int mes, long ultimoSequencial) {
        this.ano = (short) ano;
        this.mes = (short) mes;
        this.ultimoSequencial = ultimoSequencial;
    }

    public long incrementar() {
        this.ultimoSequencial++;
        return this.ultimoSequencial;
    }

    public Short getAno() {
        return ano;
    }

    public Short getMes() {
        return mes;
    }

    public Long getUltimoSequencial() {
        return ultimoSequencial;
    }
}
