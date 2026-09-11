package br.edu.safeplace.backend.domain.epi.exception;

import java.time.LocalDate;

public class CertificadoAprovacaoVencidoException extends RuntimeException {
    private final Integer epiId;
    private final String numeroCa;
    private final LocalDate dataValidadeCa;

    public CertificadoAprovacaoVencidoException(Integer epiId, String nome, String numeroCa, LocalDate dataValidadeCa) {
        super(String.format(
                "Certificado de Aprovação (CA) vencido para o EPI '%s' (ID: %s, CA: %s, Validade: %s). Manutenção bloqueada.",
                nome, epiId != null ? epiId : "N/A", numeroCa, dataValidadeCa
        ));
        this.epiId = epiId;
        this.numeroCa = numeroCa;
        this.dataValidadeCa = dataValidadeCa;
    }

    public CertificadoAprovacaoVencidoException(String message) {
        super(message);
        this.epiId = null;
        this.numeroCa = null;
        this.dataValidadeCa = null;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public String getNumeroCa() {
        return numeroCa;
    }

    public LocalDate getDataValidadeCa() {
        return dataValidadeCa;
    }
}
