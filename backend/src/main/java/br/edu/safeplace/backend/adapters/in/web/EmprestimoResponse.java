/*
 * Resposta HTTP de um empréstimo de EPI: posse, prazos, devolução, termo de cautela e a
 * classificação vigente (INDISPONIVEL, DEVOLUCAO_ATRASADA ou DISPONIVEL).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Empréstimo de EPI a um colaborador")
public record EmprestimoResponse(
        @Schema(description = "Identificador do empréstimo", example = "1") Integer id,
        @Schema(description = "Código do EPI emprestado", example = "1") Integer epiId,
        @Schema(description = "Nome do EPI") String epiNome,
        @Schema(description = "Identificador do colaborador em posse", example = "3") Integer colaboradorId,
        @Schema(description = "Nome do colaborador em posse") String colaboradorNome,
        @Schema(description = "Usuário que registrou a entrega") Integer responsavelEntregaId,
        @Schema(description = "Data e hora da entrega") LocalDateTime dataRetirada,
        @Schema(description = "Data prevista de devolução") LocalDate dataDevolucaoPrevista,
        @Schema(description = "Data e hora da devolução, quando houve") LocalDateTime dataDevolucaoReal,
        @Schema(description = "Usuário que registrou a devolução") Integer responsavelDevolucaoId,
        @Schema(description = "Estado do item na devolução") EstadoDevolucao estadoDevolucao,
        @Schema(description = "Classificação vigente do empréstimo") ClassificacaoEmprestimo status,
        @Schema(description = "Indica devolução atrasada") boolean atrasado,
        @Schema(description = "Termo de cautela registrado na entrega") String termoCautela,
        @Schema(description = "Justificativa quando substituiu item do mesmo tipo em posse") String justificativaSubstituicao) {

    public static EmprestimoResponse fromOutputDTO(EmprestimoOutputDTO dto) {
        return new EmprestimoResponse(dto.id(), dto.epiId(), dto.epiNome(), dto.colaboradorId(),
                dto.colaboradorNome(), dto.responsavelEntregaId(), dto.dataRetirada(),
                dto.dataDevolucaoPrevista(), dto.dataDevolucaoReal(), dto.responsavelDevolucaoId(),
                dto.estadoDevolucao(), dto.status(), dto.atrasado(), dto.termoCautela(),
                dto.justificativaSubstituicao());
    }
}
