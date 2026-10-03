/*
 * Resposta HTTP do registro de devolução de EPI (UC12, cenário alternativo I).
 * Reflete o destino aprovado (ESTOQUE, MANUTENCAO, DESCARTE) e o saldo persistido do EPI.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do registro de devolução de EPI refletindo destino aprovado e saldo persistido")
public record DevolucaoResponse(
        @Schema(description = "Identificador do empréstimo encerrado", example = "1") Integer emprestimoId,
        @Schema(description = "Código do EPI devolvido", example = "1") Integer epiId,
        @Schema(description = "Nome do EPI devolvido", example = "Capacete de Segurança") String epiNome,
        @Schema(description = "Identificador do colaborador que devolveu o EPI", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador", example = "João da Silva") String colaboradorNome,
        @Schema(description = "Estado do equipamento na devolução (APTO, MANUTENCAO ou DESCARTE)", example = "APTO") EstadoDevolucao estadoDevolucao,
        @Schema(description = "Destino aprovado do item (ESTOQUE, MANUTENCAO ou DESCARTE)", example = "ESTOQUE") String destinoAprovado,
        @Schema(description = "Saldo em estoque do EPI persistido após a devolução", example = "5") Integer saldoPersistido,
        @Schema(description = "Data e hora em que a devolução foi registrada") LocalDateTime dataDevolucaoReal,
        @Schema(description = "Identificador do responsável que registrou a devolução") Integer responsavelDevolucaoId) {

    public static DevolucaoResponse fromOutputDTO(EmprestimoOutputDTO dto) {
        String destino = dto.destinoAprovado();
        if (destino == null && dto.estadoDevolucao() != null) {
            destino = switch (dto.estadoDevolucao()) {
                case APTO -> "ESTOQUE";
                case MANUTENCAO -> "MANUTENCAO";
                case DESCARTE -> "DESCARTE";
            };
        }
        return new DevolucaoResponse(
                dto.id(),
                dto.epiId(),
                dto.epiNome(),
                dto.colaboradorId(),
                dto.colaboradorNome(),
                dto.estadoDevolucao(),
                destino,
                dto.saldoPersistido(),
                dto.dataDevolucaoReal(),
                dto.responsavelDevolucaoId());
    }
}
