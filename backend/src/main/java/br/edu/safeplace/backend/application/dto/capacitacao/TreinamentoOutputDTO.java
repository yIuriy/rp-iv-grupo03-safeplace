package br.edu.safeplace.backend.application.dto.capacitacao;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Treinamento com os atributos do diagrama de classes (nome, conteudo, instrutor, dataRealizacao,
 * cargaHoraria) e a situação definida em US10 (realizado ou pendente).
 */
@Schema(description = "Treinamento vinculado a um colaborador (UC02)")
public record TreinamentoOutputDTO(
        @Schema(description = "Identificador do treinamento", example = "1") Integer id,
        @Schema(description = "Nome do treinamento", example = "NR-10 Segurança em Eletricidade") String nome,
        @Schema(description = "Conteúdo ministrado") String conteudo,
        @Schema(description = "Instrutor responsável") String instrutor,
        @Schema(description = "Data de realização", example = "2026-03-10") LocalDate dataRealizacao,
        @Schema(description = "Carga horária em horas", example = "40") Integer cargaHoraria,
        @Schema(description = "Situação do treinamento", allowableValues = {"REALIZADO", "PENDENTE"}) String status
) {
    public static TreinamentoOutputDTO deDominio(Treinamento treinamento) {
        return new TreinamentoOutputDTO(
                treinamento.getId(),
                treinamento.getNome(),
                treinamento.getConteudo(),
                treinamento.getInstrutor(),
                treinamento.getDataRealizacao(),
                treinamento.getCargaHoraria(),
                treinamento.verificarStatus());
    }
}
