package br.edu.safeplace.backend.application.dto.output;

/**
 * DTO para reportar falhas ou inconsistências em EPIs que impediram o cálculo da projeção
 * (UC10 Exceção I). Permite ao gestor retificar os dados e solicitar nova tentativa.
 */
public record FalhaProjecaoOutputDTO(
        Integer epiId,
        String nomeEpi,
        String motivoFalha
) {
}
