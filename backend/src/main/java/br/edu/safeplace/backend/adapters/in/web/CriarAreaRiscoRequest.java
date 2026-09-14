package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para cadastro de nova área de risco (UC03)")
public record CriarAreaRiscoRequest(
        @NotBlank(message = "Código é obrigatório") @Schema(description = "Código identificador do setor", example = "SET-01") String codigo,

        @NotBlank(message = "Nome é obrigatório") @Schema(description = "Nome do setor físico", example = "Linha de Produção") String nome,

        @Schema(description = "Agentes de risco e limites físicos mapeados", example = "Setor de solda e montagem") String descricao,

        @NotNull(message = "Nível de perigo é obrigatório") @Schema(description = "Grau de perigo do setor", example = "ALTO") NivelPerigo nivelPerigo,

        @NotEmpty(message = "A área de risco exige ao menos um EPI obrigatório para acesso") @Schema(description = "IDs dos EPIs obrigatórios para acesso ao setor", example = "[1, 2]") List<Integer> episObrigatoriosIds) {

    public CadastrarAreaRiscoInputDTO toInputDTO() {
        return new CadastrarAreaRiscoInputDTO(codigo, nome, descricao, nivelPerigo, episObrigatoriosIds);
    }
}
