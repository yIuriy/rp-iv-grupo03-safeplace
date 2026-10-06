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

        @Schema(description = "Setor cuja periculosidade é aplicada a todos os EPIs da análise; inexistente gera erro 400. "
                + "Se omitido, cada EPI usa o setor cujo nome corresponde à sua localização.", example = "1")
        Integer setorId,

        @Schema(description = "Dias do período de amostragem da taxa de uso. Se omitido, o período vai da primeira "
                + "retirada registrada do EPI até a data de referência.", example = "90")
        Integer diasAmostragem
) {
    public LocalDate obterDataReferencia() {
        return dataReferencia != null ? dataReferencia : LocalDate.now();
    }

    /** Sem valor padrão arbitrado (DS10, 2.3): nulo indica período derivado do histórico. */
    public Integer obterDiasAmostragem() {
        return diasAmostragem != null && diasAmostragem > 0 ? diasAmostragem : null;
    }
}
