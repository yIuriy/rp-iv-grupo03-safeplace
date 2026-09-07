package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representação detalhada de um EPI")
public record EpiResponse(
        @Schema(description = "Identificador único do EPI", example = "1")
        Integer id,

        @Schema(description = "Nome do equipamento", example = "Capacete de Segurança com Aba Frontal")
        String nome,

        @Schema(description = "Número do CA", example = "12345")
        String numeroCa,

        @Schema(description = "Quantidade em estoque atual", example = "25")
        int quantidade,

        @Schema(description = "Estoque mínimo configurado", example = "5")
        int estoqueMinimo,

        @Schema(description = "Indica se o estoque está crítico (saldo <= estoqueMinimo)", example = "false")
        boolean estoqueCritico,

        @Schema(description = "Status atual do EPI", example = "DISPONIVEL")
        StatusEpi status,

        @Schema(description = "Data de validade do CA", example = "2027-12-31")
        LocalDate dataValidadeCa,

        @Schema(description = "Vida útil estimada em dias", example = "365")
        Integer vidaUtilDias
) {
    public static EpiResponse fromDomain(Epi epi) {
        return new EpiResponse(
                epi.getId(),
                epi.getNome(),
                epi.getNumeroCa(),
                epi.getQuantidade(),
                epi.getEstoqueMinimo(),
                epi.isEstoqueCritico(),
                epi.getStatus(),
                epi.getDataValidadeCa(),
                epi.getVidaUtilDias()
        );
    }

    public static EpiResponse fromOutputDTO(br.edu.safeplace.backend.application.dto.output.EpiOutputDTO dto) {
        return new EpiResponse(
                dto.id(),
                dto.nome(),
                dto.numeroCa(),
                dto.quantidade(),
                dto.estoqueMinimo(),
                dto.estoqueCritico(),
                dto.status(),
                dto.dataValidadeCa(),
                dto.vidaUtilDias()
        );
    }
}
