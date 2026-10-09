package br.edu.safeplace.backend.application.dto.capacitacao;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.capacitacao.StatusTreinamento;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Treinamento vencido ou próximo do vencimento, com o colaborador que o realizou")
public record AlertaTreinamentoOutputDTO(
        @Schema(description = "Identificador do treinamento", example = "1") Integer treinamentoId,
        @Schema(description = "Nome do treinamento", example = "NR-10 Segurança em Eletricidade") String nome,
        @Schema(description = "Situação calculada na data da consulta") StatusTreinamento status,
        @Schema(description = "Data de realização") LocalDate dataRealizacao,
        @Schema(description = "Data de vencimento") LocalDate dataVencimento,
        @Schema(description = "Identificador do colaborador", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador") String colaboradorNome
) {
    public static AlertaTreinamentoOutputDTO deDominio(Treinamento treinamento, StatusTreinamento status,
                                                       Integer colaboradorId, String colaboradorNome) {
        return new AlertaTreinamentoOutputDTO(
                treinamento.getId(),
                treinamento.getNome(),
                status,
                treinamento.getDataRealizacao(),
                treinamento.getDataVencimento().orElseThrow(),
                colaboradorId,
                colaboradorNome);
    }
}
