package br.edu.safeplace.backend.application.dto.input;

import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record AtualizarAreaRiscoInputDTO(
        String nome,
        String descricao,
        NivelPerigo nivelPerigo,
        Integer setorId,
        List<String> agentesRisco,
        String limites,
        List<Integer> episObrigatoriosIds,
        String justificativa
) {
}
