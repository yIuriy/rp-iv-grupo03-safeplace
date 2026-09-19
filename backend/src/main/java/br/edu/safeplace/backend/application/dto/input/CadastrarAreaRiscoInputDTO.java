package br.edu.safeplace.backend.application.dto.input;

import java.util.Collections;
import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record CadastrarAreaRiscoInputDTO(
        String codigo,
        String nome,
        String descricao,
        NivelPerigo nivelPerigo,
        Integer setorId,
        List<String> agentesRisco,
        String limites,
        List<Integer> episObrigatoriosIds) {

    public CadastrarAreaRiscoInputDTO(
            String codigo,
            String nome,
            String descricao,
            NivelPerigo nivelPerigo,
            List<Integer> episObrigatoriosIds) {
        this(codigo, nome, descricao, nivelPerigo, null, Collections.emptyList(), null, episObrigatoriosIds);
    }
}
