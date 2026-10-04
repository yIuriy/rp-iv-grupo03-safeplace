package br.edu.safeplace.backend.application.dto.capacitacao;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Certificação com os atributos do diagrama de classes (nome, status, dataVencimento, dataEmissao).
 */
@Schema(description = "Certificação de um colaborador (UC02)")
public record CertificacaoOutputDTO(
        @Schema(description = "Identificador da certificação", example = "1") Integer id,
        @Schema(description = "Nome da certificação", example = "NR-35 Trabalho em Altura") String nome,
        @Schema(description = "Situação calculada na data da consulta") StatusCertificacao status,
        @Schema(description = "Data de vencimento", example = "2027-01-31") LocalDate dataVencimento,
        @Schema(description = "Data de emissão", example = "2026-01-31") LocalDate dataEmissao
) {
    public static CertificacaoOutputDTO deDominio(Certificacao certificacao) {
        return new CertificacaoOutputDTO(
                certificacao.getId(),
                certificacao.getNome(),
                certificacao.getStatus(),
                certificacao.getDataVencimento(),
                certificacao.getDataEmissao());
    }
}
