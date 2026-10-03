package br.edu.safeplace.backend.application.dto.input;

import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record CadastrarTarefaInputDTO(
        String descricao,
        NivelPerigo nivelPerigo,
        Integer areaRiscoId,
        List<Integer> setoresIds) {

    public CadastrarTarefaInputDTO(String descricao, NivelPerigo nivelPerigo) {
        this(descricao, nivelPerigo, null, List.of());
    }
}
