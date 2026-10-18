package br.edu.safeplace.backend.domain.ocorrencia;

import java.util.Arrays;

import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;

/** Especializações de {@link Ocorrencia} aceitas no relato e na consulta (RF13, RF16). */
public enum TipoOcorrencia {
    ACIDENTE,
    INCIDENTE;

    /** Lê o tipo informado pelo cliente; valor ausente ou desconhecido é recusado. */
    public static TipoOcorrencia de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DadosOcorrenciaInvalidosException("Tipo de ocorrência é obrigatório: ACIDENTE ou INCIDENTE.");
        }
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new DadosOcorrenciaInvalidosException(
                        "Tipo de ocorrência inválido: " + valor.trim() + ". Use ACIDENTE ou INCIDENTE."));
    }
}
