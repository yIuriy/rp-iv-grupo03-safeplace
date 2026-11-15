package br.edu.safeplace.backend.validacao.ocorrencia;

import java.time.LocalDate;
import java.util.Objects;

import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

/**
 * Uma ação nova do plano, com a ocorrência a que pertence e a data do cadastro (UC07). O prazo da
 * ação é comparado com a data da ocorrência, que a ação guarda só pelo id, por isso a validação
 * recebe as duas.
 *
 * @param hoje data do cadastro, vinda do relógio do caso de uso (obrigatória desde a issue #308)
 */
public record AcaoDoPlano(PlanoDeAcao acao, Ocorrencia ocorrencia, LocalDate hoje) {

    public AcaoDoPlano {
        Objects.requireNonNull(hoje, "A data do cadastro da ação é obrigatória.");
    }
}
