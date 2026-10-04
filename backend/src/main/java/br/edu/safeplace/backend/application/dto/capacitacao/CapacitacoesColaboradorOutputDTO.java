package br.edu.safeplace.backend.application.dto.capacitacao;

import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detalhes das capacitações de um colaborador (UC02, passos 4 e 5; US10, critério 1), com as
 * pendências dos cenários alternativos I e III.
 */
@Schema(description = "Certificações, treinamentos e pendências de um colaborador (UC02)")
public record CapacitacoesColaboradorOutputDTO(
        @Schema(description = "Identificador do colaborador", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador") String colaboradorNome,
        @Schema(description = "Situação mais crítica entre as certificações; nula quando não há certificação cadastrada")
        StatusCertificacao situacaoCertificacoes,
        @Schema(description = "Certificações cadastradas, da que vence primeiro para a última")
        List<CertificacaoOutputDTO> certificacoes,
        @Schema(description = "Treinamentos vinculados, do mais recente para o mais antigo")
        List<TreinamentoOutputDTO> treinamentos,
        @Schema(description = "Certificações vencidas que necessitam de renovação")
        List<CertificacaoOutputDTO> certificacoesVencidas,
        @Schema(description = "Certificações que vencem dentro da antecedência de alerta")
        List<CertificacaoOutputDTO> certificacoesProximasDoVencimento,
        @Schema(description = "Indica pendência da trilha inicial de integração e de treinamentos admissionais")
        boolean pendenteIntegracaoAdmissional,
        @Schema(description = "Mensagens de alerta e pendência para exibição")
        List<String> pendencias
) {
}
