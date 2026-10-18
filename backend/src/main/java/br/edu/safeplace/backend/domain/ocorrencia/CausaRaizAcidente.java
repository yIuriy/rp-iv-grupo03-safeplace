package br.edu.safeplace.backend.domain.ocorrencia;

import java.util.Arrays;

import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;

public enum CausaRaizAcidente {
    FATOR_HUMANO,
    FALHA_EPI,
    OUTRO;

    /** Lê a causa informada pelo cliente; valor desconhecido é recusado, nunca vira OUTRO (issue #195). */
    public static CausaRaizAcidente de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DadosOcorrenciaInvalidosException(
                    "Causa raiz é obrigatória para acidentes: FATOR_HUMANO, FALHA_EPI ou OUTRO.");
        }
        return Arrays.stream(values())
                .filter(causa -> causa.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new DadosOcorrenciaInvalidosException(
                        "Causa raiz inválida: " + valor.trim() + ". Use FATOR_HUMANO, FALHA_EPI ou OUTRO."));
    }
}
