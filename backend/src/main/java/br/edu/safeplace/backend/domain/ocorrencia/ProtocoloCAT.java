package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;

public final class ProtocoloCAT {
    private static final Pattern PADRAO_CAT = Pattern.compile("^CAT-\\d{4}-\\d{2}-\\d{4,}$");

    private final String valor;

    private ProtocoloCAT(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ProtocoloCATInvalidoException("Protocolo CAT não pode ser nulo ou vazio.");
        }
        String valorLimpo = valor.trim().toUpperCase();
        if (!PADRAO_CAT.matcher(valorLimpo).matches()) {
            throw new ProtocoloCATInvalidoException("Formato de protocolo CAT inválido: " + valor);
        }
        this.valor = valorLimpo;
    }

    public static ProtocoloCAT de(String valor) {
        return new ProtocoloCAT(valor);
    }

    public static ProtocoloCAT gerar(LocalDateTime dataReferencia, long sequencial) {
        if (sequencial <= 0) {
            throw new ProtocoloCATInvalidoException("Número sequencial deve ser positivo.");
        }
        LocalDateTime data = dataReferencia != null ? dataReferencia : LocalDateTime.now();
        String formatado = String.format("CAT-%d-%02d-%04d", data.getYear(), data.getMonthValue(), sequencial);
        return new ProtocoloCAT(formatado);
    }

    public static ProtocoloCAT gerar(long sequencial) {
        return gerar(LocalDateTime.now(), sequencial);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtocoloCAT that = (ProtocoloCAT) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
