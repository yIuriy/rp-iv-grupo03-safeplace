package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarSetorRequest(
        @NotBlank(message = "Nome do setor é obrigatório.")
        String nome,
        String descricao,
        String localizacao,
        @NotNull(message = "Status/Nível de perigo do setor é obrigatório.")
        NivelPerigo status
) {
    public CadastrarSetorInputDTO toInputDTO() {
        return new CadastrarSetorInputDTO(nome, descricao, localizacao, status);
    }
}
