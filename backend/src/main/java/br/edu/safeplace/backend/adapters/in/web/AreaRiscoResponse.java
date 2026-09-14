package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de uma área de risco do mapa global de riscos")
public record AreaRiscoResponse(
        @Schema(description = "Identificador único da área de risco", example = "1") Integer id,

        @Schema(description = "Código identificador do setor", example = "SET-01") String codigo,

        @Schema(description = "Nome do setor físico", example = "Linha de Produção") String nome,

        @Schema(description = "Agentes de risco e limites físicos mapeados") String descricao,

        @Schema(description = "Grau de perigo do setor", example = "ALTO") NivelPerigo nivelPerigo,

        @Schema(description = "EPIs obrigatórios para acesso ao setor") List<EpiResumoResponse> episObrigatorios) {

    public static AreaRiscoResponse fromOutputDTO(AreaRiscoOutputDTO dto) {
        return new AreaRiscoResponse(
                dto.id(),
                dto.codigo(),
                dto.nome(),
                dto.descricao(),
                dto.nivelPerigo(),
                dto.episObrigatorios().stream()
                        .map(EpiResumoResponse::fromOutputDTO)
                        .toList());
    }
}
