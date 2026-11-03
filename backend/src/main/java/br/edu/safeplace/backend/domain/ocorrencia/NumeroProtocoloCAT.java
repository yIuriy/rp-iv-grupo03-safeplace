package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import jakarta.persistence.Embeddable;

/**
 * Value Object do número de protocolo da CAT (US13). É guardado dentro de {@link Acidente} (issue #230)
 * e mapeado como {@code @Embeddable} na mesma coluna de antes, conforme a decisão de JPA no domínio
 * (#254, seção 2.2). Dois protocolos com o mesmo valor normalizado são iguais.
 *
 * <p>O VO normaliza o texto e informa se o formato é válido, mas não recusa nada na criação: a
 * recusa é um elo do {@code OcorrenciaValidador}, que roda antes do repository (issue #266).</p>
 */
@Embeddable
public record NumeroProtocoloCAT(String valor) {
    private static final Pattern PADRAO_CAT = Pattern.compile("^CAT-\\d{4}-\\d{2}-\\d{4,}$");

    /** Normaliza o texto (sem espaços nas pontas e em maiúsculas); não valida o formato. */
    public NumeroProtocoloCAT {
        valor = valor != null ? valor.trim().toUpperCase() : null;
    }

    /** Verdadeiro quando o valor segue o formato CAT-AAAA-MM-NNNN. */
    public boolean formatoValido() {
        return valor != null && PADRAO_CAT.matcher(valor).matches();
    }

    /** Verdadeiro quando não há texto algum, ou seja, o protocolo ainda não foi emitido. */
    public boolean semValor() {
        return valor == null || valor.isEmpty();
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
