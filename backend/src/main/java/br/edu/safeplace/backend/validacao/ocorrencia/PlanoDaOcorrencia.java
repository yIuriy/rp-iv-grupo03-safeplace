package br.edu.safeplace.backend.validacao.ocorrencia;

import java.util.List;

import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

/**
 * O plano de ação de uma ocorrência: as ações já gravadas somadas às que serão salvas (UC07).
 *
 * @param acoes nula vale como lista vazia
 */
public record PlanoDaOcorrencia(Ocorrencia ocorrencia, List<PlanoDeAcao> acoes) {

    public PlanoDaOcorrencia {
        acoes = acoes != null ? List.copyOf(acoes) : List.of();
    }
}
