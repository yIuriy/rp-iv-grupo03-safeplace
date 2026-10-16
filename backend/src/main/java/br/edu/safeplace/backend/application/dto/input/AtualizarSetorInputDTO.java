package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record AtualizarSetorInputDTO(
        String nome,
        String descricao,
        String localizacao,
        NivelPerigo status
) {
}
