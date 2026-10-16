package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exigências normativas de capacitação e EPIs obrigatórios para execução de uma tarefa (US06, critério 4)")
public record ExigenciasTarefaResponse(
        @Schema(description = "ID da tarefa", example = "1")
        Integer tarefaId,

        @Schema(description = "Descrição da atividade operacional", example = "Solda em altura na estrutura metálica")
        String descricao,

        @Schema(description = "Grau de periculosidade classificado", example = "ALTO")
        NivelPerigo nivelPerigo,

        @Schema(description = "Indica se a tarefa está classificada", example = "true")
        boolean classificada,

        @Schema(description = "Exigências de capacitação e treinamentos normativos")
        List<ItemExigenciaCapacitacaoResponse> capacitacoes,

        @Schema(description = "EPIs obrigatórios para a tarefa")
        List<EpiResponse> episObrigatorios
) {
    public record ItemExigenciaCapacitacaoResponse(
            Integer id,
            String nomeCapacitacao,
            ExigenciaCapacitacao.TipoCapacitacao tipoCapacitacao
    ) {
        public static ItemExigenciaCapacitacaoResponse deDominio(ExigenciaCapacitacao exigencia) {
            return new ItemExigenciaCapacitacaoResponse(
                    exigencia.getId(),
                    exigencia.getNomeCapacitacao(),
                    exigencia.getTipoCapacitacao()
            );
        }
    }
}
