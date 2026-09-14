package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record CadastrarTarefaInputDTO(
        String descricao,
        NivelPerigo nivelPerigo) {
}
