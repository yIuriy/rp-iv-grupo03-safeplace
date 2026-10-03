package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Parâmetros de entrada para solicitação da Análise de Projeção de Desgaste (UC10 / DS10).
 */
@Schema(description = "Parâmetros para geração da análise de projeção de desgaste de EPIs")
public record GerarAnaliseProjecaoInputDTO(
        @Schema(description = "Data de referência para o cálculo (padrão: hoje)", example = "2026-10-02")
        LocalDate dataReferencia,

        @Schema(description = "Identificador do setor para filtrar a análise ou sobrescrever periculosidade", example = "1")
        Integer setorId,

        @Schema(description = "Dias de amostragem histórica de empréstimos (padrão: 90 dias)", example = "90")
        Integer diasAmostragem
) {
    public LocalDate obterDataReferencia() {
        return dataReferencia != null ? dataReferencia : LocalDate.now();
    }

    public int obterDiasAmostragem() {
        return diasAmostragem != null && diasAmostragem > 0 ? diasAmostragem : 90;
    }
}
