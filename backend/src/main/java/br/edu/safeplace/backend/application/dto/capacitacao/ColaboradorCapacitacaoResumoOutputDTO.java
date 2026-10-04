package br.edu.safeplace.backend.application.dto.capacitacao;

import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Linha da listagem de colaboradores do UC02 (passo 2 e cenário alternativo IV). Identifica a
 * pessoa só por id e nome: nenhuma credencial do beneficiário é exposta ou exigida (RNF03).
 */
@Schema(description = "Situação de capacitação de um colaborador na listagem do UC02")
public record ColaboradorCapacitacaoResumoOutputDTO(
        @Schema(description = "Identificador do colaborador", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador") String colaboradorNome,
        @Schema(description = "Situação mais crítica entre as certificações; nula quando não há certificação cadastrada")
        StatusCertificacao situacaoCertificacoes,
        @Schema(description = "Indica ausência de certificações cadastradas (UC02, cenário alternativo III)")
        boolean semCertificacoes,
        @Schema(description = "Total de certificações cadastradas") int totalCertificacoes,
        @Schema(description = "Total de treinamentos vinculados") int totalTreinamentos,
        @Schema(description = "Indica pendência da trilha inicial de integração e de treinamentos admissionais")
        boolean pendenteIntegracaoAdmissional
) {
}
