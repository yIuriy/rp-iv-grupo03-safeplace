package br.edu.safeplace.backend.application.dto.capacitacao;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Certificação vencida ou próxima do vencimento, com o colaborador titular")
public record AlertaCertificacaoOutputDTO(
        @Schema(description = "Identificador da certificação", example = "1") Integer certificacaoId,
        @Schema(description = "Nome da certificação", example = "NR-35 Trabalho em Altura") String nome,
        @Schema(description = "Situação calculada na data da consulta") StatusCertificacao status,
        @Schema(description = "Data de vencimento") LocalDate dataVencimento,
        @Schema(description = "Data de emissão") LocalDate dataEmissao,
        @Schema(description = "Identificador do colaborador titular", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador titular") String colaboradorNome
) {
    public static AlertaCertificacaoOutputDTO deDominio(Certificacao certificacao, String colaboradorNome) {
        return new AlertaCertificacaoOutputDTO(
                certificacao.getId(),
                certificacao.getNome(),
                certificacao.getStatus(),
                certificacao.getDataVencimento(),
                certificacao.getDataEmissao(),
                certificacao.getColaboradorId(),
                colaboradorNome);
    }
}
