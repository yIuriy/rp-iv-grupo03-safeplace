package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de uma área de risco do mapa global de riscos")
public record AreaRiscoResponse(
        @Schema(description = "Identificador único da área de risco", example = "1") Integer id,

        @Schema(description = "Código identificador da área de risco", example = "SET-01") String codigo,

        @Schema(description = "Nome da área de risco", example = "Linha de Produção") String nome,

        @Schema(description = "Descrição da área de risco") String descricao,

        @Schema(description = "Grau de perigo do setor", example = "ALTO") NivelPerigo nivelPerigo,

        @Schema(description = "ID do setor físico ao qual pertence", example = "1") Integer setorId,

        @Schema(description = "Lista de agentes de risco identificados") List<String> agentesRisco,

        @Schema(description = "Limites físicos ou operacionais mapeados") String limites,

        @Schema(description = "EPIs obrigatórios para acesso ao setor") List<EpiResumoResponse> episObrigatorios) {

    public static AreaRiscoResponse fromOutputDTO(AreaRiscoOutputDTO dto) {
        if (dto == null) {
            return null;
        }
        return new AreaRiscoResponse(
                dto.id(),
                dto.codigo(),
                dto.nome(),
                dto.descricao(),
                dto.nivelPerigo(),
                dto.setorId(),
                dto.agentesRisco(),
                dto.limites(),
                dto.episObrigatorios() != null ? dto.episObrigatorios().stream()
                        .map(EpiResumoResponse::fromOutputDTO)
                        .toList() : List.of());
    }
}
