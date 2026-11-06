package br.edu.safeplace.backend.domain.ocorrencia;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave primária composta da sequência anual/mensal do protocolo CAT (#261).
 */
public class SequenciaProtocoloCatId implements Serializable {

    private Short ano;
    private Short mes;

    public SequenciaProtocoloCatId() {
    }

    public SequenciaProtocoloCatId(Short ano, Short mes) {
        this.ano = ano;
        this.mes = mes;
    }

    public Short getAno() {
        return ano;
    }

    public Short getMes() {
        return mes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequenciaProtocoloCatId that = (SequenciaProtocoloCatId) o;
        return Objects.equals(ano, that.ano) && Objects.equals(mes, that.mes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ano, mes);
    }
}
