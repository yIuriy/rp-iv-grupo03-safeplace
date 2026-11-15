package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/** Valida o registro de manutenção antes de gravá-lo (RF04, UC01). Antes da issue #264, no construtor. */
@Component
public class ManutencaoEpiValidador extends ValidadorEmCadeia<ManutencaoEpi> {

    public ManutencaoEpiValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<ManutencaoEpi> montarCadeia() {
        EloValidacao<ManutencaoEpi> primeiro = exigir(m -> m.getEpiId() == null, "Identificador do EPI é obrigatório.");
        primeiro.encadear(exigir(m -> m.getDataManutencao() == null, "Data da manutenção é obrigatória."))
                .encadear(exigir(m -> isBlankOrNull(m.getDescricao()), "Descrição da manutenção é obrigatória."))
                .encadear(exigir(m -> m.getResultado() == null, "Resultado da manutenção é obrigatório."))
                .encadear(exigir(m -> isBlankOrNull(m.getResponsavelManutencao()), "Responsável pela manutenção é obrigatório."));
        return primeiro;
    }
}
