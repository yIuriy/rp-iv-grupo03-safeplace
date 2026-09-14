package br.edu.safeplace.backend.application.dto.input;

import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record CadastrarAreaRiscoInputDTO(
        String codigo,
        String nome,
        String descricao,
        NivelPerigo nivelPerigo,
        List<Integer> episObrigatoriosIds) {
}
