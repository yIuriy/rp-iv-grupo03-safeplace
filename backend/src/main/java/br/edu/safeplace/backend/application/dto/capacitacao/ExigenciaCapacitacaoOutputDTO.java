package br.edu.safeplace.backend.application.dto.capacitacao;

import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao.TipoAlvo;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao.TipoCapacitacao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exigência de capacitação para uma tarefa ou EPI (UC02)")
public record ExigenciaCapacitacaoOutputDTO(
        @Schema(description = "Identificador da exigência", example = "1") Integer id,
        @Schema(description = "Nome da certificação ou do treinamento exigido") String nomeCapacitacao,
        TipoCapacitacao tipoCapacitacao,
        TipoAlvo alvo,
        @Schema(description = "Identificador da tarefa ou do EPI exigente", example = "5") Integer alvoId
) {
    public static ExigenciaCapacitacaoOutputDTO deDominio(ExigenciaCapacitacao exigencia) {
        return new ExigenciaCapacitacaoOutputDTO(
                exigencia.getId(),
                exigencia.getNomeCapacitacao(),
                exigencia.getTipoCapacitacao(),
                exigencia.getAlvo(),
                exigencia.getAlvoId());
    }
}
