package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

/**
 * Parâmetros de entrada para solicitação da Análise de Projeção de Desgaste (UC10 / DS10).
 */
public record GerarAnaliseProjecaoInputDTO(
        LocalDate dataReferencia,
        Integer setorId,
        Integer diasAmostragem
) {
    public LocalDate obterDataReferencia() {
        return dataReferencia != null ? dataReferencia : LocalDate.now();
    }

    public int obterDiasAmostragem() {
        return diasAmostragem != null && diasAmostragem > 0 ? diasAmostragem : 90;
    }
}
