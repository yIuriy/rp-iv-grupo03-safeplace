package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;

/**
 * Value Object (Record) que padroniza o número de protocolo da CAT para acidentes.
 */
public record NumeroProtocoloCAT(String valor) {
    private static final Pattern PADRAO_CAT = Pattern.compile("^CAT-\\d{4}-\\d{2}-\\d{4,}$");

    /** Normaliza o texto; o formato é validado pelo OcorrenciaValidador antes de salvar (issue #266). */
    public NumeroProtocoloCAT {
        valor = valor != null ? valor.trim().toUpperCase() : null;
    }

    /** Verdadeiro quando o valor segue o formato CAT-AAAA-MM-NNNN. */
    public boolean formatoValido() {
        return valor != null && PADRAO_CAT.matcher(valor).matches();
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
