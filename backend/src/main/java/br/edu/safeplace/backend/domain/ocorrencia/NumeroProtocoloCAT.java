package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;

/**
 * Value Object (Record) que padroniza o número de protocolo da CAT para acidentes.
 */
public record NumeroProtocoloCAT(String valor) {
    private static final Pattern PADRAO_CAT = Pattern.compile("^CAT-\\d{4}-\\d{2}-\\d{4,}$");

    public NumeroProtocoloCAT {
        if (valor == null || valor.isBlank()) {
            throw new ProtocoloCATInvalidoException("Protocolo CAT não pode ser nulo ou vazio.");
        }
        valor = valor.trim().toUpperCase();
        if (!PADRAO_CAT.matcher(valor).matches()) {
            throw new ProtocoloCATInvalidoException("Formato de protocolo CAT inválido: " + valor);
        }
    }

    public static NumeroProtocoloCAT de(String valor) {
        return new NumeroProtocoloCAT(valor);
    }

    public static NumeroProtocoloCAT gerar(LocalDateTime dataReferencia, long sequencial) {
        if (sequencial <= 0) {
            throw new ProtocoloCATInvalidoException("Número sequencial deve ser positivo.");
        }
        LocalDateTime data = dataReferencia != null ? dataReferencia : LocalDateTime.now();
        String formatado = String.format("CAT-%d-%02d-%04d", data.getYear(), data.getMonthValue(), sequencial);
        return new NumeroProtocoloCAT(formatado);
    }

    public static NumeroProtocoloCAT gerar(long sequencial) {
        return gerar(LocalDateTime.now(), sequencial);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return valor;
    }
}
