package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um setor físico da planta industrial")
public record SetorResponse(
        @Schema(description = "Identificador único do setor", example = "1") Integer id,
        @Schema(description = "Nome do setor físico", example = "Galpão A") String nome,
        @Schema(description = "Descrição das atividades e características do setor", example = "Área de caldeiraria e montagem pesada") String descricao,
        @Schema(description = "Localização física na planta", example = "Bloco 2, Piso Térreo") String localizacao,
        @Schema(description = "Status ou nível de perigo geral associado ao setor", example = "MEDIO") NivelPerigo status
) {
    public static SetorResponse fromOutputDTO(SetorOutputDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SetorResponse(
                dto.id(),
                dto.nome(),
                dto.descricao(),
                dto.localizacao(),
                dto.status()
        );
    }
}
